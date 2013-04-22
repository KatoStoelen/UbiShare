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
import android.util.SparseArray;

/**
 * Sync manager for WiFi Direct.
 * 
 * @author Kato
 */
class WiFiDirectSyncManager extends P2PSyncManager {
	
	/**
	 * A map containing error messages of each error code.
	 */
	private static final SparseArray<String> errorMessagesWifiDirect;
	
	static {
		errorMessagesWifiDirect = new SparseArray<String>();
		errorMessagesWifiDirect.append(WifiP2pManager.ERROR, "INTERNAL ERROR");
		errorMessagesWifiDirect.append(WifiP2pManager.P2P_UNSUPPORTED, "P2P_UNSUPPORTED");
		errorMessagesWifiDirect.append(WifiP2pManager.BUSY, "BUSY");
	}
	
	private final WifiP2pManager mWifiP2pManager;
	private final Channel mChannel;

	/**
	 * Initializes a new WiFi Direct sync manager.
	 * @param context The context to use.
	 * @param p2pListener The listener to notify of P2P changes.
	 */
	public WiFiDirectSyncManager(Context context, IP2PListener p2pListener) {
		super(context, ConnectionType.WIFI_DIRECT, p2pListener);
		
		mWifiP2pManager = (WifiP2pManager) context.getSystemService(Context.WIFI_P2P_SERVICE);
		mChannel = mWifiP2pManager.initialize(context, context.getMainLooper(), null);
	}
	
	/**
	 * Starts the sync client using WiFi Direct.
	 * @param groupOwnerAddress The address of the group owner.
	 */
	private void startSyncClient(InetSocketAddress groupOwnerAddress) {
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
	}

	@Override
	protected BroadcastReceiver getBroadcastReceiver() {
		return new WiFiDirectBroadcastReceiver(
				mP2pListener, mConnectionListener, mWifiP2pManager, mChannel);
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
				mP2pListener.onDiscoverPeersFailure(
						errorMessagesWifiDirect.get(reason),
						ConnectionType.WIFI_DIRECT);
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
				mP2pListener.onConnectFailure(
						errorMessagesWifiDirect.get(reason),
						ConnectionType.WIFI_DIRECT);
			}
		});
	}

	@Override
	protected P2PConnectionListener getServerConnectionListener() {
		return new WiFiDirectConnectionListener(
				P2PConstants.WIFI_DIRECT_SERVER_PORT);
	}
	
	/**
	 * Listener handling notifications regarding the creation of a
	 * WiFi Direct group.
	 */
	private final ConnectionInfoListener mConnectionListener =
			new ConnectionInfoListener() {
		public void onConnectionInfoAvailable(WifiP2pInfo info) {
			if (info.groupFormed && info.isGroupOwner) {
				startSyncServer();
				
				mP2pListener.onSuccessfulConnection(
						SyncRole.SERVER, ConnectionType.WIFI_DIRECT);
			} else if (info.groupFormed) {
				InetAddress groupOwnerAddress = info.groupOwnerAddress;
				InetSocketAddress socketAddress = new InetSocketAddress(
						groupOwnerAddress, P2PConstants.WIFI_DIRECT_SERVER_PORT);
				
				startSyncClient(socketAddress);
				
				mP2pListener.onSuccessfulConnection(
						SyncRole.CLIENT, ConnectionType.WIFI_DIRECT);
			}
		}
	};
}
