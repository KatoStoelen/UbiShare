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

import java.util.UUID;

import org.societies.android.p2p.P2PConnection.ConnectionType;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.util.Log;

/**
 * The P2PSyncManager handles the creation of WiFi Direct groups
 * and pairing of devices.
 * 
 * @author Kato
 */
public abstract class P2PSyncManager {
	
	public static final String TAG = "P2PSyncManager";

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
	
	private final Object mTerminationLock = new Object();
	
	private final ConnectionType mConnectionType;
	private IntentFilter mIntentFilter;
	private BroadcastReceiver mBroadcastReceiver;
	private ServiceConnection mServiceConnection;
	
	protected Context mContext;
	protected final IP2PListener mP2pListener;
	
	/**
	 * Initializes a new P2P Sync Manager.
	 * @param context The context to use.
	 * @param connectionType The type of connection to use.
	 * @param p2pListener The P2P listener.
	 */
	protected P2PSyncManager(
			Context context,
			ConnectionType connectionType,
			IP2PListener p2pListener) {
		mContext = context;
		mConnectionType = connectionType;
		mIntentFilter = getIntentFilter();
		mBroadcastReceiver = getBroadcastReceiver();
		mServiceConnection = getServiceConnection();
		
		mP2pListener = p2pListener;
	}
	
	/**
	 * Gets the broadcast receiver of the specified connection type.
	 * @return A broadcast receiver.
	 */
	protected abstract BroadcastReceiver getBroadcastReceiver();

	/**
	 * Gets the intent filter of the specified connection type.
	 * @return An intent filter.
	 */
	protected abstract IntentFilter getIntentFilter();
	
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
	public abstract void discoverPeers();
	
	/**
	 * Starts the connection to the specified device. This is an asynchronous call.
	 * @param device The device to connect to.
	 */
	public abstract void connectTo(P2PDevice device);
	
	/**
	 * Gets the connection listener used to accept incoming connections when
	 * acting as a sync server.
	 * @return The connection listener of the sync server.
	 */
	protected abstract P2PConnectionListener getServerConnectionListener();
	
	/**
	 * Starts the sync server.
	 */
	protected void startSyncServer() {
		try {
			stopSync(true);
			
			P2PConnectionListener listener = getServerConnectionListener();
			
			Intent intent = new Intent(mContext, P2PSyncServerService.class);
			intent.putExtra(
					P2PSyncServerService.EXTRA_CONNECTION_LISTENER,
					listener);
			
			mContext.startService(intent);
		} catch (InterruptedException e) {
			Log.e(TAG, "Could not start sync server: Interrupted while " +
					"awaiting sync server termination");
		}
	}
	
	/**
	 * Gets a unique ID that can be used to identify a client.
	 * @return A string containing a unique ID.
	 */
	protected String getUniqueId() {
		SharedPreferences preferences =
				mContext.getSharedPreferences(
						P2PConstants.PREFERENCE_FILE, Context.MODE_PRIVATE);
		
		String uniqueId = preferences.getString(
				P2PConstants.PREFERENCE_UNIQUE_ID, null);
		
		if (uniqueId == null) {
			uniqueId = UUID.randomUUID().toString();
			
			preferences.edit()
				.putString(P2PConstants.PREFERENCE_UNIQUE_ID, uniqueId)
				.commit();
		}
		
		return uniqueId;
	}
	
	/**
	 * Stops the synchronization.
	 * @param awaitTermination Whether or not to block until the
	 * synchronization has terminated.
	 * @throws InterruptedException If the thread is interrupted while awaiting
	 * termination.
	 */
	public void stopSync(boolean awaitTermination) throws InterruptedException {
		Intent intent = null;
		
		if (P2PSyncClientService.IS_RUNNING)
			intent = new Intent(mContext, P2PSyncClientService.class);
		else if (P2PSyncServerService.IS_RUNNING)
			intent = new Intent(mContext, P2PSyncServerService.class);
		
		if (intent != null) {
			mContext.bindService(
					intent, mServiceConnection, Context.BIND_AUTO_CREATE);
			
			if (awaitTermination) {
				synchronized (mTerminationLock) {
					mTerminationLock.wait();
				}
			}
		}
	}

	/**
	 * Gets the connection type currently being used.
	 * @return The connection type in use.
	 */
	public ConnectionType getConnectionType() {
		return mConnectionType;
	}
	
	/**
	 * Gets the service connection.
	 * @return The service connection.
	 */
	private ServiceConnection getServiceConnection() {
		return new ServiceConnection() {
			public void onServiceDisconnected(ComponentName name) { }
			public void onServiceConnected(ComponentName name, IBinder service) {
				LocalServiceBinder localBinder = (LocalServiceBinder) service;
				
				ISyncService syncService = localBinder.getService();
				syncService.stopSync(true);
				
				synchronized (mTerminationLock) {
					mTerminationLock.notify();
				}
				
				mContext.unbindService(this);
			}
		};
	}
	
	/**
	 * Gets the sync manager with the specified connection type.
	 * @param context The context to use.
	 * @param listener The listener to notify of P2P changes.
	 * @param connectionType The connection type to use.
	 * @return An initialized <code>P2PSyncManager</code> instance.
	 */
	public static P2PSyncManager getSyncManager(
			Context context,
			IP2PListener listener,
			ConnectionType connectionType) {
		if (connectionType == ConnectionType.WIFI_DIRECT)
			return new WiFiDirectSyncManager(context, listener);
		else if (connectionType == ConnectionType.BLUETOOTH)
			return new BluetoothSyncManager(context, listener);
		else
			throw new IllegalArgumentException("Unknown connection type: "
					+ connectionType);
	}
}
