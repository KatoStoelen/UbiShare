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

import org.societies.android.p2p.P2PConnection.ConnectionType;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
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
	private void startSyncClient(InetSocketAddress groupOwnerAddress) {
		try {
			stopSync(true);
			
			Intent intent = new Intent(mContext, P2PSyncClientService.class);
			intent.putExtra(
					P2PSyncClientService.EXTRA_CONNECTION,
					new WiFiDirectConnection(groupOwnerAddress));
			intent.putExtra(
					P2PSyncClientService.EXTRA_LISTENER,
					new WiFiDirectConnectionListener(
							P2PConstants.WIFI_DIRECT_CLIENT_PORT));
			intent.putExtra(P2PSyncClientService.EXTRA_UNIQUE_ID, getUniqueId());
			
			mContext.startService(intent);
		} catch (InterruptedException e) {
			Log.e(TAG, "Could not start sync client: Interrupted while " +
					"awaiting sync client termination");
		}
	}

	@Override
	protected BroadcastReceiver getBroadcastReceiver() {
		return new WiFiDirectBroadcastReceiver(this, mWifiP2pManager, mChannel);
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
		mWifiP2pManager.discoverPeers(mChannel, new ActionListener() {
			public void onSuccess() { /* Deliberately empty */ }

			public void onFailure(int reason) {
				mChangeListener.onDiscoverPeersFailure(
						errorMessagesWifiDirect.get(reason),
						WiFiDirectSyncManager.this);
			}
		});
	}

	@Override
	public void connectTo(P2PDevice device) {
		WifiP2pConfig config = new WifiP2pConfig();
		config.deviceAddress = device.getAddress();
		
		mWifiP2pManager.connect(mChannel, config, new ActionListener() {
			public void onSuccess() { /* Deliberately empty */ }
			
			public void onFailure(int reason) {
				mChangeListener.onConnectionFailure(
						errorMessagesWifiDirect.get(reason),
						WiFiDirectSyncManager.this);
			}
		});
	}
	
	@Override
	public void disconnect() {
		mWifiP2pManager.removeGroup(mChannel, new ActionListener() {
			public void onSuccess() {
				mChangeListener.onDisconnectSuccess(WiFiDirectSyncManager.this);
			}
			
			public void onFailure(int reason) {
				mChangeListener.onDisconnectFailure(
						errorMessagesWifiDirect.get(reason),
						WiFiDirectSyncManager.this);
			}
		});
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
		if (info.groupFormed && info.isGroupOwner) {
			startSyncServer();
			
			mChangeListener.onConnectionSuccess(SyncRole.SERVER, this);
		} else if (info.groupFormed) {
			InetAddress groupOwnerAddress = info.groupOwnerAddress;
			InetSocketAddress socketAddress = new InetSocketAddress(
					groupOwnerAddress, P2PConstants.WIFI_DIRECT_SERVER_PORT);
			
			startSyncClient(socketAddress);
			
			mChangeListener.onConnectionSuccess(SyncRole.CLIENT, this);
		}
	}
}
