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
package org.societies.android.p2p.service;

import org.societies.android.p2p.ConnectionType;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.NetworkInfo;
import android.net.wifi.p2p.WifiP2pManager;

/**
 * Broadcast receiver used to notify sync services of WiFi Direct connection
 * loss. This way sync services can gracefully shut down when the WiFi Direct
 * connection is broken.
 * 
 * @author Kato
 */
class WiFiDirectServiceBroadcastReceiver extends ServiceBroadcastReceiver {

	/**
	 * Initializes a new WiFi Direct service broadcast receiver.
	 * @param syncService The sync service.
	 */
	protected WiFiDirectServiceBroadcastReceiver(ISyncService syncService) {
		super(ConnectionType.WIFI_DIRECT, syncService);
	}

	@Override
	public void onReceive(Context context, Intent intent) {
		String action = intent.getAction();
		
		if (WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION.equals(action)) {
			int state = intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE, -1);
			
	        if (state == WifiP2pManager.WIFI_P2P_STATE_DISABLED)
	        	stopSyncService();
		} else if (WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION.equals(action)) {
			NetworkInfo networkInfo = (NetworkInfo) intent.getParcelableExtra(
            		WifiP2pManager.EXTRA_NETWORK_INFO);

            if (!networkInfo.isConnected())
                stopSyncService();
		}
	}

	@Override
	public IntentFilter getIntentFilter() {
		IntentFilter filter = new IntentFilter();
		
		filter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
		filter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
		
		return filter;
	}
}
