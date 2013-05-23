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

import org.societies.android.p2p.entity.P2PDevice;
import org.societies.android.p2p.net.P2PConnectionListener;
import org.societies.android.p2p.service.ISyncService;
import org.societies.android.p2p.service.LocalServiceBinder;
import org.societies.android.p2p.service.P2PSyncClientService;
import org.societies.android.p2p.service.P2PSyncServerService;

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
 * The P2P sync manager is the glue holding all the pieces of the peer-to-peer
 * synchronization together. All actions related to peer-to-peer synchronization
 * is performed through this class.
 * <br><br>
 * The constructor is <code>protected</code>, which enforces the use of
 * <code>P2PSyncManager.getSyncManager(Context, IP2PChangeListener, ConnecionType)</code>.
 * <br><br>
 * The P2P sync manager support multiple instances, as long as the connection type is
 * different. Multiple instances of <code>P2PSyncManager</code> with the same
 * connection type is not allowed. Two instances of <code>P2PSyncManager</code> can
 * be used to allow synchronization using both Bluetooth and WiFi Direct etc.
 * 
 * @see P2PSyncManager#getSyncManager(Context, IP2PChangeListener, ConnectionType)
 * @author Kato
 */
public abstract class P2PSyncManager {
	
	public static final String TAG = "P2PSyncManager";
	
	/**
	 * P2P interface statuses are used to reflect the state of a
	 * peer-to-peer interface, whether it is on or off, or even if
	 * it is not supported.
	 */
	public enum P2PInterfaceStatus {
		/** Indicates that P2P is not supported on this device. */
		NOT_SUPPORTED,
		
		/** Indicates that the P2P interface is OFF. */
		OFF,
		
		/** Indicates that the P2P interface is ON. */
		ON
	}
	
	/**
	 * Synchronization roles are used to indicate whether the current
	 * device is acting as a server or a client.
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
	private boolean mBroadcastReceiverRegistered = false;
	
	protected final IP2PChangeListener mChangeListener;
	protected final Context mContext;
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
	private void initialize() {
		mIntentFilter = getIntentFilter();
		mBroadcastReceiver = getBroadcastReceiver();
		mServiceConnection = getServiceConnection();
		
		mInitialized = true;
	}
	
	/**
	 * Throws an <code>IllegalStateException</code> it the sync manager is
	 * in an illegal state.
	 * @throws IllegalStateException If the sync manager is in an illegal state.
	 */
	protected void throwIfIllegalState() throws IllegalStateException {
		if (!mInitialized)
			throw new IllegalStateException("Not initialized: Call initialize()");
		if (!mBroadcastReceiverRegistered)
			throw new IllegalStateException("Broadcast receiver not registered");
	}
	
	/**
	 * Gets the broadcast receiver used to receive notifications of interface
	 * and connectivity changes.
	 * @return A broadcast receiver.
	 */
	protected abstract BroadcastReceiver getBroadcastReceiver();

	/**
	 * Gets the intent filter used to determine which broadcasts to receive.
	 * @return An intent filter.
	 */
	protected abstract IntentFilter getIntentFilter();
	
	/**
	 * The broadcast receiver is used to receive notifications of interface and
	 * connectivity changes. Registering the broadcast receiver is necessary in
	 * order for the P2P sync manager to work. This method should be called
	 * within the activity's <code>onResume()</code> method.
	 */
	public void registerBroadcastReceiver() {
		mContext.registerReceiver(mBroadcastReceiver, mIntentFilter);

		mBroadcastReceiverRegistered = true;
	}
	
	/**
	 * Unregistering the broadcast receiver will cut down on unnecessary system
	 * overhead and Android does not allow a previously registered broadcast
	 * receiver to continue after the activity has been suspended. An exception
	 * is thrown if the broadcast receiver is not unregistered when the activity
	 * goes into a suspended state. This method should be called within the
	 * activity's <code>onPause()</code> method.
	 */
	public void unregisterBroadcastReceiver() {
		mContext.unregisterReceiver(mBroadcastReceiver);
		
		mBroadcastReceiverRegistered = false;
	}
	
	/**
	 * Starts the discovering of peers. This is an asynchronous call. The
	 * P2P change listener will receive a notification when peers are available.
	 * @see IP2PChangeListener#onPeersAvailable(java.util.List, boolean, Object)
	 * @see IP2PChangeListener#onDiscoverPeersFailure(String, Object)
	 */
	public abstract void discoverPeers();
	
	/**
	 * Starts the connection to the specified device. This is an asynchronous call.
	 * The P2P change listener will receive a notification when the connection has
	 * been made.
	 * @param device The device to connect to.
	 * @see IP2PChangeListener#onConnectionSuccess(SyncRole, Object)
	 * @see IP2PChangeListener#onConnectionFailure(String, Object)
	 */
	public abstract void connectTo(P2PDevice device);
	
	/**
	 * Disconnects from the P2P group. This is an asynchronous call. The P2P change
	 * listener will receive a notification when disconnected.
	 * @see IP2PChangeListener#onDisconnectSuccess(Object)
	 * @see IP2PChangeListener#onDisconnectFailure(String, Object)
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
	 * Starts the sync server service. If the synchronization is already running,
	 * it will be stopped and restarted.
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
	 * Gets a unique ID that can be used to identify a client. The unique
	 * ID is stored in shared preferences for reuse.
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
		
		if (P2PSyncClientService.isSyncClientRunning(mConnectionType))
			intent = new Intent(mContext, P2PSyncClientService.class);
		else if (P2PSyncServerService.isSyncServerRunning(mConnectionType))
			intent = new Intent(mContext, P2PSyncServerService.class);
		
		if (intent != null) {
			mContext.bindService(
					intent, mServiceConnection, Context.BIND_AUTO_CREATE);
			
			if (awaitTermination) {
				synchronized (mTerminationLock) {
					mTerminationLock.wait();
				}
			}
		} else {
			Log.i(TAG, "Synchronization not running!");
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
	 * Gets whether or not the synchronization is currently active.
	 * @return <code>true</code> if either sync server or sync client is running,
	 * otherwise <code>false</code>.
	 */
	public boolean isSynchronizationActive() {
		return P2PSyncClientService.isSyncClientRunning(mConnectionType) ||
				P2PSyncServerService.isSyncServerRunning(mConnectionType);
	}
	
	/**
	 * Gets the service connection used to stop synchronization services.
	 * @return A <code>ServiceConnection</code> instance.
	 */
	private ServiceConnection getServiceConnection() {
		return new ServiceConnection() {
			public void onServiceDisconnected(ComponentName name) { }
			public void onServiceConnected(ComponentName name, IBinder service) {
				LocalServiceBinder localBinder = (LocalServiceBinder) service;
				
				ISyncService syncService = localBinder.getService();
				syncService.stopSync(mConnectionType, true);
				
				mContext.unbindService(this);
				
				synchronized (mTerminationLock) {
					mTerminationLock.notifyAll();
				}
			}
		};
	}
	
	/**
	 * Gets the sync manager with the specified connection type.
	 * @param context The context to use.
	 * @param listener The listener to notify of P2P changes.
	 * @param connectionType The connection type to use.
	 * @return An initialized <code>P2PSyncManager</code> instance.
	 * @see ConnectionType
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
			throw new IllegalArgumentException(
					"Unknown connection type: " + connectionType);
		
		syncManager.initialize();
		
		return syncManager;
	}
}
