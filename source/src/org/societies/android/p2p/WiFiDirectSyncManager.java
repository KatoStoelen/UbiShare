/**
 * Copyright 2013 UbiCollab
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.societies.android.p2p;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.List;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.WpsInfo;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.WifiP2pManager.ActionListener;
import android.net.wifi.p2p.WifiP2pManager.Channel;
import android.net.wifi.p2p.WifiP2pManager.ConnectionInfoListener;
import android.util.Log;
import android.util.SparseArray;

/**
 * Sync manager for WiFi Direct.
 * 
 * @author Kato
 */
class WiFiDirectSyncManager extends P2PSyncManager implements ConnectionInfoListener {
	
	public static final String TAG = "WiFiDirectSyncManager";
	
	/**
	 * A map containing error messages mapped with error codes.
	 */
	private static final SparseArray<String> errorMessagesWifiDirect;
	
	static {
		errorMessagesWifiDirect = new SparseArray<String>();
		errorMessagesWifiDirect.append(WifiP2pManager.ERROR, "INTERNAL ERROR");
		errorMessagesWifiDirect.append(WifiP2pManager.P2P_UNSUPPORTED, "P2P UNSUPPORTED");
		errorMessagesWifiDirect.append(WifiP2pManager.BUSY, "BUSY");
	}
	
	private final WifiP2pManager mWifiP2pManager;
	private final Channel mChannel;
	
	private boolean mInGroup = false;

	/**
	 * Initializes a new WiFi Direct sync manager.
	 * @param context The context to use.
	 * @param p2pListener The listener to notify of P2P changes.
	 */
	public WiFiDirectSyncManager(Context context, IP2PChangeListener p2pListener) {
		super(context, ConnectionType.WIFI_DIRECT, p2pListener);
		
		mWifiP2pManager = (WifiP2pManager) context.getSystemService(Context.WIFI_P2P_SERVICE);
		mChannel = mWifiP2pManager.initialize(context, context.getMainLooper(), null);
	}
	
	/**
	 * Starts the sync client using WiFi Direct.
	 * @param groupOwnerAddress The address of the group owner.
	 */
	private void startSyncClient(final InetSocketAddress groupOwnerAddress) {
		new Thread(new Runnable() { /* Avoid NotworkOnMainThreadException */
			public void run() {
				Log.i(TAG, "Starting Sync Client...");
				
				Intent intent = new Intent(
						mContext, P2PSyncClientService.class);
				intent.putExtra(
						P2PSyncClientService.EXTRA_CONNECTION,
						new WiFiDirectConnection(groupOwnerAddress));
				intent.putExtra(
						P2PSyncClientService.EXTRA_LISTENER,
						new WiFiDirectConnectionListener(
								P2PConstants.WIFI_DIRECT_CLIENT_PORT));
				intent.putExtra(
						P2PSyncClientService.EXTRA_UNIQUE_ID, getUniqueId());
				
				mContext.startService(intent);
			}
		}).start();
	}

	@Override
	protected BroadcastReceiver getBroadcastReceiver() {
		return new WiFiDirectBroadcastReceiver(this);
	}

	@Override
	protected IntentFilter getIntentFilter() {
		IntentFilter filter = new IntentFilter();
		
		filter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
		filter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
		filter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
		filter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);
		
		return filter;
	}

	@Override
	public void discoverPeers() {
		throwIfIllegalState();
		
		Log.i(TAG, "Discovering peers...");
		
		mWifiP2pManager.discoverPeers(mChannel, new ActionListener() {
			public void onSuccess() {
				Log.i(TAG, "Peer Discovery Initiated");
			}

			public void onFailure(int reason) {
				Log.i(TAG, "Peer Discovery Failed: " + reason);
				mChangeListener.onDiscoverPeersFailure(
						errorMessagesWifiDirect.get(reason),
						WiFiDirectSyncManager.this);
			}
		});
	}

	@Override
	public void connectTo(P2PDevice device) {
		throwIfIllegalState();
		
		Log.i(TAG, "Connecting to device " + device.getName() +
				" (" + device.getAddress() + ")...");
		
		WifiP2pConfig config = new WifiP2pConfig();
		config.deviceAddress = device.getAddress();
		config.wps.setup = WpsInfo.PBC;
		
		mWifiP2pManager.connect(mChannel, config, new ActionListener() {
			public void onSuccess() {
				Log.i(TAG, "Connection Initiated");
			}
			
			public void onFailure(int reason) {
				Log.i(TAG, "Connection Failed: " + reason);
				mChangeListener.onConnectionFailure(
						errorMessagesWifiDirect.get(reason),
						WiFiDirectSyncManager.this);
			}
		});
	}
	
	@Override
	public void disconnect() {
		throwIfIllegalState();
		
		Log.i(TAG, "Disconnecting...");
		
		mWifiP2pManager.removeGroup(mChannel, new ActionListener() {
			public void onSuccess() {
				Log.i(TAG, "Disconnect Initiated");
				mChangeListener.onDisconnectSuccess(WiFiDirectSyncManager.this);
			}
			
			public void onFailure(int reason) {
				Log.i(TAG, "Disconnect Failed: " + reason);
				mChangeListener.onDisconnectFailure(
						errorMessagesWifiDirect.get(reason),
						WiFiDirectSyncManager.this);
			}
		});
	}
	
	@Override
	public boolean isConnected() {
		return mInGroup;
	}
	
	/**
	 * Gets the WiFi P2P channel.
	 * @return The WiFi P2P channel.
	 */
	public Channel getChannel() {
		return mChannel;
	}
	
	/**
	 * Gets the WiFi P2P manager.
	 * @return The WiFi P2P manager.
	 */
	public WifiP2pManager getWifiP2pManager() {
		return mWifiP2pManager;
	}
	
	/**
	 * Notifies the listener of P2P interface status change.
	 * @param status The state of the P2P interface.
	 */
	public void notifyP2pInterfaceStatusChange(P2PInterfaceStatus status) {
		mChangeListener.onP2pInterfaceStatusChange(status, this);
	}
	
	/**
	 * Notifies the listener of status changes of this device.
	 * @param device The new device status.
	 */
	public void notifyThisDeviceStatusChange(WiFiDirectP2PDevice device) {
		mChangeListener.onThisDeviceChange(device, this);
	}
	
	/**
	 * Notifies the listener of available peers.
	 * @param peers The available peers.
	 */
	public void notifyPeersAvailable(List<P2PDevice> peers) {
		mChangeListener.onPeersAvailable(peers, true, this);
	}

	@Override
	protected P2PConnectionListener getServerConnectionListener() {
		return new WiFiDirectConnectionListener(
				P2PConstants.WIFI_DIRECT_SERVER_PORT);
	}
	
	/*
	 * (non-Javadoc)
	 * @see android.net.wifi.p2p.WifiP2pManager.ConnectionInfoListener#onConnectionInfoAvailable(android.net.wifi.p2p.WifiP2pInfo)
	 */
	public void onConnectionInfoAvailable(WifiP2pInfo info) {
		Log.i(TAG, "Group created: " + info.groupFormed);
		
		mInGroup = info.groupFormed;
		
		if (!isSynchronizationActive()) {
			if (mInGroup && info.isGroupOwner) {
				startSyncServer();
				
				mChangeListener.onConnectionSuccess(SyncRole.SERVER, this);
			} else if (mInGroup) {
				InetAddress groupOwnerAddress = info.groupOwnerAddress;
				InetSocketAddress socketAddress = new InetSocketAddress(
						groupOwnerAddress, P2PConstants.WIFI_DIRECT_SERVER_PORT);
				
				startSyncClient(socketAddress);
				
				mChangeListener.onConnectionSuccess(SyncRole.CLIENT, this);
			}
		}
	}
}
