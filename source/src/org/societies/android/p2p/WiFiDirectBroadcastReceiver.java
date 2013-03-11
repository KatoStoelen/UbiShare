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

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.WifiP2pManager.ConnectionInfoListener;
import android.net.wifi.p2p.WifiP2pManager.PeerListListener;

/**
 * A broadcast receiver for Wi-Fi Direct.
 * 
 * @author Kato
 */
class WiFiDirectBroadcastReceiver extends BroadcastReceiver {
	
	private IP2PListener mP2pListener;
	private ConnectionInfoListener mConnectionListener;
	
	/**
	 * Initializes a new WiFi Direct broadcast receiver.
	 * @param p2pListener The P2P listener.
	 * @param connectionListener The connection listener.
	 */
	public WiFiDirectBroadcastReceiver(
			IP2PListener p2pListener, ConnectionInfoListener connectionListener) {
		mP2pListener = p2pListener;
		mConnectionListener = connectionListener;
	}

	@Override
	public void onReceive(Context context, Intent intent) {
		String action = intent.getAction();
		
		if (WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION.equals(action)) {
			// TODO: check if P2P is on
		} else if (WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION.equals(action)) {
			// TODO: update peer list
		} else if (WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION.equals(action)) {
			// TODO: request connection info
		} else if (WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION.equals(action)) {
			// TODO: update info of current device
		}
	}
	
	private final PeerListListener mPeerListListener = new PeerListListener() {
		public void onPeersAvailable(WifiP2pDeviceList peers) {
			List<P2PDevice> devices = new ArrayList<P2PDevice>();
			
			for (WifiP2pDevice device : peers.getDeviceList())
				devices.add(new WiFiDirectP2PDevice(device));
			
			mP2pListener.onPeersAvailable(devices, true);
		}
	};
}
