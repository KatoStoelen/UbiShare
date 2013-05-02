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
	
	protected final IP2PChangeListener mChangeListener;
	protected Context mContext;
	protected boolean mInitialized = false;
	
	/**
	 * Creates a new P2P Sync Manager instance. A call to
	 * <code>initialize()</code> is required before using the sync
	 * manager.
	 * @param context The context to use.
	 * @param connectionType The type of connection to use.
	 * @param changeListener The listener to notify of P2P changes.
	 * @see P2PSyncManager#initialize()
	 * @see P2PSyncManager#getSyncManager(Context, IP2PChangeListener, ConnectionType)
	 */
	protected P2PSyncManager(
			Context context,
			ConnectionType connectionType,
			IP2PChangeListener changeListener) {
		mContext = context;
		mConnectionType = connectionType;
		mChangeListener = changeListener;
	}
	
	/**
	 * Initializes the sync manager.
	 */
	protected void initialize() {
		mIntentFilter = getIntentFilter();
		mBroadcastReceiver = getBroadcastReceiver();
		mServiceConnection = getServiceConnection();
		
		mInitialized = true;
	}
	
	/**
	 * Throws an <code>IllegalStateException</code> it the sync manager is
	 * not initialized.
	 * @throws IllegalStateException If the sync manager is not initialized.
	 */
	protected void throwIfNotInitialized() throws IllegalStateException {
		if (!mInitialized)
			throw new IllegalStateException("Not initialized: Call initialize()");
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
		Log.i(TAG, "Broadcast Receiver Registered");
	}
	
	/**
	 * Unregisters the broadcast receiver.
	 */
	public void unregisterBroadcastReceiver() {
		mContext.unregisterReceiver(mBroadcastReceiver);
		Log.i(TAG, "Broadcast Receiver Unregistered");
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
	 * Disconnects from the P2P group.
	 */
	public abstract void disconnect();
	
	/**
	 * Gets whether or not this device is currently connected to a peer-to-peer
	 * network.
	 * @return <code>true</code> if the device is connected to a peer-to-peer
	 * network, otherwise <code>false</code>.
	 */
	public abstract boolean isConnected();
	
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
		Log.i(TAG, "Starting Sync Server...");
		
		try {
			if (isSynchronizationActive())
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
	protected void stopSync(boolean awaitTermination) throws InterruptedException {
		Log.i(TAG, "Stopping Synchronization...");
		
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
	 * Stops the synchronization. This is an asynchronous call.
	 * <code>IP2PChangeListener.onSyncStopped()</code> will be called when
	 * the synchronization has stopped.
	 * @see IP2PChangeListener#onSyncStopped(Object)
	 */
	public void stopSync() {
		try {
			stopSync(false);
		} catch (InterruptedException e) { /* Ignore */ }
	}

	/**
	 * Gets the connection type currently being used.
	 * @return The connection type in use.
	 */
	public ConnectionType getConnectionType() {
		return mConnectionType;
	}
	
	/**
	 * Gets whether or not the synchronization is currently active.
	 * @return <code>true</code> if either sync server or sync client is running,
	 * otherwise <code>false</code>.
	 */
	public boolean isSynchronizationActive() {
		return P2PSyncClientService.IS_RUNNING || P2PSyncServerService.IS_RUNNING;
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
				
				mContext.unbindService(this);
				
				synchronized (mTerminationLock) {
					mTerminationLock.notifyAll();
				}
				
				mChangeListener.onSyncStopped(P2PSyncManager.this);
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
			IP2PChangeListener listener,
			ConnectionType connectionType) {
		P2PSyncManager syncManager = null;
		
		if (connectionType == ConnectionType.WIFI_DIRECT)
			syncManager = new WiFiDirectSyncManager(context, listener);
		else if (connectionType == ConnectionType.BLUETOOTH)
			syncManager = new BluetoothSyncManager(context, listener);
		else
			throw new IllegalArgumentException("Unknown connection type: "
					+ connectionType);
		
		syncManager.initialize();
		
		return syncManager;
	}
}
