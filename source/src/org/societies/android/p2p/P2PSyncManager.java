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

import org.societies.android.p2p.P2PConnection.ConnectionType;

import android.bluetooth.BluetoothDevice;
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
 * The P2PSyncManager handles the creation of WiFi Direct groups
 * and pairing of devices.
 * 
 * @author Kato
 */
public class P2PSyncManager {

	/**
	 * An enum of P2P interface statuses.
	 */
	public enum P2PInterfaceStatus {
		/** Indicates that the P2P is not supported. */
		NOT_SUPPORTED,
		
		/** Indicates that the P2P interface is OFF. */
		OFF,
		
		/** Indicates that the P2P interface is ON. */
		ON
	}
	
	/**
	 * An enum of synchronization roles.
	 */
	public enum SyncRole {
		/** Indicates that the current device is the server. */
		SERVER,
		
		/** Indicates that the current device is a client. */
		CLIENT
	}
	
	private static final SparseArray<String> errorReasonsWifiDirect;
	
	static {
		errorReasonsWifiDirect = new SparseArray<String>();
		errorReasonsWifiDirect.append(WifiP2pManager.ERROR, "INTERNAL ERROR");
		errorReasonsWifiDirect.append(WifiP2pManager.P2P_UNSUPPORTED, "P2P_UNSUPPORTED");
		errorReasonsWifiDirect.append(WifiP2pManager.BUSY, "BUSY");
	}
	
	private ConnectionType mConnectionType;
	private WifiP2pManager mWifiP2pManager;
	private IntentFilter mIntentFilter;
	private Context mContext;
	private BroadcastReceiver mBroadcastReceiver;
	private Channel mChannel;
	
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
		mChannel = mWifiP2pManager.initialize(context, context.getMainLooper(), null);
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
			return new WiFiDirectBroadcastReceiver(
					mP2pListener, mConnectionListener, mWifiP2pManager, mChannel);
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
	
	/**
	 * Starts the discovering of peers. This is an asynchronous call.
	 */
	public void discoverPeers() {
		if (mConnectionType == ConnectionType.WIFI_DIRECT)
			discoverPeersWifiDirect();
		else if (mConnectionType == ConnectionType.BLUETOOTH)
			discoverPeersBluetooth();
	}
	
	/**
	 * Starts the discovering of peers using WiFi Direct.
	 */
	private void discoverPeersWifiDirect() {
		mWifiP2pManager.discoverPeers(mChannel, new ActionListener() {
			
			public void onSuccess() { /* Deliberately empty */ }

			public void onFailure(int reason) {
				mP2pListener.onDiscoverPeersFailure(
						errorReasonsWifiDirect.get(reason), ConnectionType.WIFI_DIRECT);
			}
		});
	}
	
	/**
	 * Starts the discovering of peers using Bluetooth.
	 */
	private void discoverPeersBluetooth() {
		// TODO: IMPLEMENT
	}
	
	/**
	 * Starts the connection to the specified device. This is an asynchronous call.
	 * @param device The device to connect to.
	 */
	public void connectTo(P2PDevice device) {
		if (device instanceof WiFiDirectP2PDevice)
			connectToWifiDirectDevice((WiFiDirectP2PDevice) device);
		else if (device instanceof BluetoothP2PDevice)
			connectToBluetoothDevice((BluetoothP2PDevice) device);
	}
	
	/**
	 * Starts the connection to the specified WiFi Direct device.
	 * @param device The device to connect to.
	 */
	private void connectToWifiDirectDevice(WiFiDirectP2PDevice device) {
		WifiP2pConfig config = new WifiP2pConfig();
		config.deviceAddress = device.getAddress();
		
		mWifiP2pManager.connect(mChannel, config, new ActionListener() {
			
			public void onSuccess() { /* Deliberately empty */ }
			
			public void onFailure(int reason) {
				mP2pListener.onConnectFailure(
						errorReasonsWifiDirect.get(reason), ConnectionType.WIFI_DIRECT);
			}
		});
	}
	
	/**
	 * Starts the connection to the specified Bluetooth device.
	 * @param device The device to connect to.
	 */
	private void connectToBluetoothDevice(BluetoothP2PDevice device) {
		// TODO: IMPLEMENT
	}
	
	/**
	 * Starts the sync server.
	 */
	private void startSyncServer(ConnectionType connectionType) {
		stopSync(true);
		
		ConnectionListener listener = null;
		if (connectionType == ConnectionType.WIFI_DIRECT)
			listener = new WiFiDirectConnectionListener(
					P2PConstants.WIFI_DIRECT_SERVER_PORT);
		else if (connectionType == ConnectionType.BLUETOOTH)
			listener = new BluetoothConnectionListener();
		
		Intent intent = new Intent(mContext, P2PSyncServerService.class);
		intent.putExtra(
				P2PSyncServerService.EXTRA_CONNECTION_LISTENER,
				listener);
		
		mContext.startService(intent);
	}
	
	/**
	 * Starts the sync client using WiFi Direct.
	 * @param groupOwnerAddress The address of the group owner.
	 */
	private void startWifiDirectSyncClient(InetAddress groupOwnerAddress) {
		stopSync(true);
		
		Intent intent = new Intent(mContext, P2PSyncClientService.class);
		intent.putExtra(
				P2PSyncClientService.EXTRA_CONNECTION,
				new WiFiDirectConnection(groupOwnerAddress));
		intent.putExtra(
				P2PSyncClientService.EXTRA_LISTENER,
				new WiFiDirectConnectionListener(
						P2PConstants.WIFI_DIRECT_CLIENT_PORT));
		
		mContext.startService(intent);
	}
	
	/**
	 * Stops the synchronization.
	 * @param awaitTermination Whether or not to block until the
	 * synchronization has terminated.
	 */
	public void stopSync(boolean awaitTermination) {
		// TODO: Implement
	}
	
	private final ConnectionInfoListener mConnectionListener = new ConnectionInfoListener() {
		public void onConnectionInfoAvailable(WifiP2pInfo info) {
			if (info.groupFormed && info.isGroupOwner) {
				startSyncServer(ConnectionType.WIFI_DIRECT);
				
				mP2pListener.onSuccessfulConnection(
						SyncRole.SERVER, ConnectionType.WIFI_DIRECT);
			} else if (info.groupFormed) {
				InetAddress groupOwnerAddress = info.groupOwnerAddress;
				startWifiDirectSyncClient(groupOwnerAddress);
				
				mP2pListener.onSuccessfulConnection(
						SyncRole.CLIENT, ConnectionType.WIFI_DIRECT);
			}
		}
	};
}
