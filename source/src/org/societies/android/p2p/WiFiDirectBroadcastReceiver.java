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

import java.util.ArrayList;
import java.util.List;

import org.societies.android.p2p.P2PSyncManager.ConnectionStatus;
import org.societies.android.p2p.P2PSyncManager.ConnectionType;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.NetworkInfo;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.WifiP2pManager.Channel;
import android.net.wifi.p2p.WifiP2pManager.ConnectionInfoListener;
import android.net.wifi.p2p.WifiP2pManager.PeerListListener;

/**
 * A broadcast receiver for Wi-Fi Direct.
 * 
 * @author Kato
 */
class WiFiDirectBroadcastReceiver extends BroadcastReceiver {
	
	private IP2PListener mP2pListener;
	private WifiP2pManager mManager;
	private ConnectionInfoListener mConnectionListener;
	private Channel mChannel;
	
	/**
	 * Initializes a new WiFi Direct broadcast receiver.
	 * @param p2pListener The P2P listener.
	 * @param connectionListener The connection listener.
	 * @param manager The WifiP2pManager instance.
	 * @param channel The channel instance.
	 */
	public WiFiDirectBroadcastReceiver(
			IP2PListener p2pListener,
			ConnectionInfoListener connectionListener,
			WifiP2pManager manager,
			Channel channel) {
		mP2pListener = p2pListener;
		mConnectionListener = connectionListener;
		mManager = manager;
		mChannel = channel;
	}

	@Override
	public void onReceive(Context context, Intent intent) {
		String action = intent.getAction();
		
		if (WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION.equals(action)) {
			int state = intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE, -1);
			
	        if (state == WifiP2pManager.WIFI_P2P_STATE_ENABLED)
	        	mP2pListener.onP2pConnectionStatusChange(
	        			ConnectionStatus.ON, ConnectionType.WIFI_DIRECT);
	        else
	        	mP2pListener.onP2pConnectionStatusChange(
	        			ConnectionStatus.OFF, ConnectionType.WIFI_DIRECT);
		} else if (WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION.equals(action)) {
			if (mManager != null)
				mManager.requestPeers(mChannel, mPeerListListener);
		} else if (WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION.equals(action)) {
			if (mManager == null)
                return;

            NetworkInfo networkInfo = (NetworkInfo) intent.getParcelableExtra(
            		WifiP2pManager.EXTRA_NETWORK_INFO);

            if (networkInfo.isConnected())
                mManager.requestConnectionInfo(mChannel, mConnectionListener);
		} else if (WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION.equals(action)) {
			WifiP2pDevice wifiDevice = (WifiP2pDevice) intent.getParcelableExtra(
                    WifiP2pManager.EXTRA_WIFI_P2P_DEVICE);
			
			mP2pListener.onThisDeviceChange(
					new WiFiDirectP2PDevice(wifiDevice), ConnectionType.WIFI_DIRECT);
		}
	}
	
	private final PeerListListener mPeerListListener = new PeerListListener() {
		public void onPeersAvailable(WifiP2pDeviceList peers) {
			List<P2PDevice> devices = new ArrayList<P2PDevice>();
			
			for (WifiP2pDevice device : peers.getDeviceList())
				devices.add(new WiFiDirectP2PDevice(device));
			
			mP2pListener.onPeersAvailable(devices, true, ConnectionType.WIFI_DIRECT);
		}
	};
}
