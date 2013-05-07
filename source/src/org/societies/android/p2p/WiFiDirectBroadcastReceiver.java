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
import android.util.Log;

/**
 * A broadcast receiver for Wi-Fi Direct.
 * 
 * @author Kato
 */
class WiFiDirectBroadcastReceiver extends BroadcastReceiver {
	
	public static final String TAG = "WiFiDirectBroadcastReceiver";
	
	private WiFiDirectSyncManager mSyncManager;
	private WifiP2pManager mWifiP2pManager;
	private Channel mChannel;
	
	/**
	 * Initializes a new WiFi Direct broadcast receiver.
	 * @param syncManager The WiFi Direct sync manager.
	 */
	public WiFiDirectBroadcastReceiver(
			WiFiDirectSyncManager syncManager) {
		mSyncManager = syncManager;
		mWifiP2pManager = syncManager.getWifiP2pManager();
		mChannel = syncManager.getChannel();
	}

	@Override
	public void onReceive(Context context, Intent intent) {
		String action = intent.getAction();
		
		if (WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION.equals(action)) {
			int state = intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE, -1);
			
			Log.i(TAG, "P2P State Changed: " + state);
			
	        if (state == WifiP2pManager.WIFI_P2P_STATE_ENABLED)
	        	mSyncManager.notifyP2pInterfaceStatusChange(P2PInterfaceStatus.ON);
	        else
	        	mSyncManager.notifyP2pInterfaceStatusChange(P2PInterfaceStatus.OFF);
		} else if (WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION.equals(action)) {
			Log.i(TAG, "Peers Changed");
			
			mWifiP2pManager.requestPeers(mChannel, mPeerListListener);
		} else if (WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION.equals(action)) {
			Log.i(TAG, "Connection Changed");
			
            NetworkInfo networkInfo = (NetworkInfo) intent.getParcelableExtra(
            		WifiP2pManager.EXTRA_NETWORK_INFO);

            if (networkInfo.isConnected())
                mWifiP2pManager.requestConnectionInfo(mChannel, mSyncManager);
		} else if (WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION.equals(action)) {
			Log.i(TAG, "This Device Changed");
			
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
			Log.i(TAG, "Peers Available: " + peers.getDeviceList().size());
			
			List<P2PDevice> devices = new ArrayList<P2PDevice>();
			
			for (WifiP2pDevice device : peers.getDeviceList())
				devices.add(new WiFiDirectP2PDevice(device));
			
			mSyncManager.notifyPeersAvailable(devices);
		}
	};
}
