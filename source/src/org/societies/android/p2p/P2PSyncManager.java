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

import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.IntentFilter;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.WifiP2pManager.ConnectionInfoListener;

/**
 * The P2PSyncManager handles the creation of WiFi Direct groups
 * and pairing of devices.
 * 
 * @author Kato
 */
public class P2PSyncManager {

	/**
	 * An enum of supported connection types.
	 */
	public enum ConnectionType {
		/** Specifies Bluetooth as communication channel. */
		BLUETOOTH,
		
		/** Specifies WiFi Direct as communication channel. */
		WIFI_DIRECT
	}
	
	/**
	 * An enum of P2P connection statuses.
	 */
	public enum ConnectionStatus {
		/** Indicates that the P2P connection is not supported. */
		NOT_SUPPORTED,
		
		/** Indicates that the P2P connection is OFF. */
		OFF,
		
		/** Indicates that the P2P connection is ON. */
		ON
	}
	
	private ConnectionType mConnectionType;
	private WifiP2pManager mWifiP2pManager;
	private IntentFilter mIntentFilter;
	private Context mContext;
	private BroadcastReceiver mBroadcastReceiver;
	
	private final IP2PListener mP2pListener;
	
	/**
	 * Initializes a new P2P Sync Manager.
	 * @param context The context to use.
	 * @param connectionType The type of connection to use.
	 * @param p2pListener The P2P listener.
	 */
	public P2PSyncManager(
			Context context, ConnectionType connectionType, IP2PListener p2pListener) {
		mContext = context;
		mConnectionType = connectionType;
		mIntentFilter = getIntentFilter(connectionType);
		mBroadcastReceiver = getBroadcastReceiver(connectionType);
		mWifiP2pManager = (WifiP2pManager) context.getSystemService(Context.WIFI_P2P_SERVICE);
		mP2pListener = p2pListener;
	}
	
	/**
	 * Gets the broadcast receiver of the specified connection type.
	 * @param connectionType The connection type in use.
	 * @return A broadcast receiver.
	 */
	private BroadcastReceiver getBroadcastReceiver(ConnectionType connectionType) {
		if (connectionType == ConnectionType.BLUETOOTH)
			return new BluetoothBroadcastReceiver();
		else if (connectionType == ConnectionType.WIFI_DIRECT)
			return new WiFiDirectBroadcastReceiver(mP2pListener, mConnectionListener);
		else
			return null;
	}

	/**
	 * Gets the intent filter of the specified connection type.
	 * @param connectionType The connection type in use.
	 * @return An intent filter.
	 */
	private IntentFilter getIntentFilter(ConnectionType connectionType) {
		IntentFilter filter = new IntentFilter();
		
		if (connectionType == ConnectionType.BLUETOOTH) {
			filter.addAction(BluetoothDevice.ACTION_FOUND);
		} else if (connectionType == ConnectionType.WIFI_DIRECT) {
			filter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
			filter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
			filter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
			filter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);
		}
		
		return filter;
	}
	
	/**
	 * Registers a broadcast receiver to be called with the connection specific
	 * broadcast intents.
	 */
	public void registerBroadcastReceiver() {
		mContext.registerReceiver(mBroadcastReceiver, mIntentFilter);
	}
	
	/**
	 * Unregisters the broadcast receiver.
	 */
	public void unregisterBroadcastReceiver() {
		mContext.unregisterReceiver(mBroadcastReceiver);
	}
	
	private final ConnectionInfoListener mConnectionListener = new ConnectionInfoListener() {
		public void onConnectionInfoAvailable(WifiP2pInfo info) {
			if (info.groupFormed && info.isGroupOwner) {
				// TODO: Start Sync Server
			} else if (info.groupFormed) {
				InetAddress groupOwnerAddress = info.groupOwnerAddress;
				// TODO: Start Sync Client
			}
		}
	};
}
