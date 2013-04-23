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

import org.societies.android.p2p.P2PSyncManager.P2PInterfaceStatus;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.NetworkInfo;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.WifiP2pManager.Channel;
import android.net.wifi.p2p.WifiP2pManager.PeerListListener;

/**
 * A broadcast receiver for Wi-Fi Direct.
 * 
 * @author Kato
 */
class WiFiDirectBroadcastReceiver extends BroadcastReceiver {
	
	private WiFiDirectSyncManager mSyncManager;
	private WifiP2pManager mWifiP2pManager;
	private Channel mChannel;
	
	/**
	 * Initializes a new WiFi Direct broadcast receiver.
	 * @param syncManager The WiFi Direct sync manager.
	 * @param wifiP2pManager The WifiP2pManager instance.
	 * @param channel The channel instance.
	 */
	public WiFiDirectBroadcastReceiver(
			WiFiDirectSyncManager syncManager,
			WifiP2pManager wifiP2pManager,
			Channel channel) {
		mSyncManager = syncManager;
		mWifiP2pManager = wifiP2pManager;
		mChannel = channel;
	}

	@Override
	public void onReceive(Context context, Intent intent) {
		String action = intent.getAction();
		
		if (WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION.equals(action)) {
			int state = intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE, -1);
			
	        if (state == WifiP2pManager.WIFI_P2P_STATE_ENABLED)
	        	mSyncManager.notifyP2pInterfaceStatusChange(P2PInterfaceStatus.ON);
	        else
	        	mSyncManager.notifyP2pInterfaceStatusChange(P2PInterfaceStatus.OFF);
		} else if (WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION.equals(action)) {
			if (mWifiP2pManager != null)
				mWifiP2pManager.requestPeers(mChannel, mPeerListListener);
		} else if (WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION.equals(action)) {
			if (mWifiP2pManager == null)
                return;

            NetworkInfo networkInfo = (NetworkInfo) intent.getParcelableExtra(
            		WifiP2pManager.EXTRA_NETWORK_INFO);

            if (networkInfo.isConnected())
                mWifiP2pManager.requestConnectionInfo(mChannel, mSyncManager);
		} else if (WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION.equals(action)) {
			WifiP2pDevice wifiDevice = (WifiP2pDevice) intent.getParcelableExtra(
                    WifiP2pManager.EXTRA_WIFI_P2P_DEVICE);
			
			mSyncManager.notifyThisDeviceStatusChange(
					new WiFiDirectP2PDevice(wifiDevice));
		}
	}
	
	/**
	 * Listener used to handle notifications of peer list changes.
	 */
	private final PeerListListener mPeerListListener = new PeerListListener() {
		public void onPeersAvailable(WifiP2pDeviceList peers) {
			List<P2PDevice> devices = new ArrayList<P2PDevice>();
			
			for (WifiP2pDevice device : peers.getDeviceList())
				devices.add(new WiFiDirectP2PDevice(device));
			
			mSyncManager.notifyPeersAvailable(devices);
		}
	};
}
