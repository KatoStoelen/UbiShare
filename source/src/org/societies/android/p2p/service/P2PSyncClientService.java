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

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.societies.android.p2p.ConnectionType;
import org.societies.android.p2p.net.P2PConnection;
import org.societies.android.p2p.net.P2PConnectionListener;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

/**
 * Service wrapper of the sync client. The use of a service enables the
 * synchronization to run in the background even if the activity is
 * suspended.
 * 
 * @author Kato
 */
public class P2PSyncClientService extends Service implements ISyncService {
	
	/** Name of the P2P connection extra. */
	public static final String EXTRA_CONNECTION = "extra_connection";
	/** Name of the connection listener extra. */
	public static final String EXTRA_LISTENER = "extra_listener";
	/** Name of the unique ID extra. */
	public static final String EXTRA_UNIQUE_ID = "extra_unique_id";
	
	private static final Map<ConnectionType, Integer> mSyncClientRunning =
			Collections.synchronizedMap(new HashMap<ConnectionType, Integer>());
	
	private final IBinder mBinder = new LocalServiceBinder(this);
	private final Map<ConnectionType, P2PSyncClient> mSyncClients =
			Collections.synchronizedMap(new HashMap<ConnectionType, P2PSyncClient>());
	private final Map<ConnectionType, ServiceBroadcastReceiver> mBroadcastReceivers =
			Collections.synchronizedMap(new HashMap<ConnectionType, ServiceBroadcastReceiver>());
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		if (intent != null) {
			P2PConnection connection = (P2PConnection)
					intent.getParcelableExtra(EXTRA_CONNECTION);
			P2PConnectionListener listener = (P2PConnectionListener)
					intent.getParcelableExtra(EXTRA_LISTENER);
			String uniqueId = intent.getStringExtra(EXTRA_UNIQUE_ID);
			
			ConnectionType connectionType = connection.getConnectionType();
			
			if (!mSyncClients.containsKey(connectionType)) {
				P2PSyncClient syncClient = new P2PSyncClient(
						uniqueId, connection, listener, this);

				startSyncClient(syncClient, connectionType);
			}
		}
		
		return START_STICKY;
	}
	
	@Override
	public IBinder onBind(Intent intent) {
		return mBinder;
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		
		stopAllSyncClients(false);
	}
	
	/**
	 * Starts the specified sync client.
	 * @param syncClient The sync client to start.
	 * @param connectionType The connection type in use.
	 */
	private void startSyncClient(
			P2PSyncClient syncClient, ConnectionType connectionType) {
		syncClient.start();
		
		mSyncClients.put(connectionType, syncClient);
		mSyncClientRunning.put(connectionType, 1);
		
		registerBroadcastReceiver(connectionType);
	}
	
	/**
	 * Registers the service broadcast receiver of the specified connection
	 * type.
	 * @param connectionType The connection type in use.
	 */
	private void registerBroadcastReceiver(ConnectionType connectionType) {
		ServiceBroadcastReceiver receiver = 
				ServiceBroadcastReceiver.getBroadcastReceiver(
						connectionType, this);
		
		registerReceiver(receiver, receiver.getIntentFilter());
		
		mBroadcastReceivers.put(connectionType, receiver);
	}
	
	/**
	 * Unregisters the service broadcast receiver or the specified connection
	 * type.
	 * @param connectionType The connection type in use.
	 */
	private void unregisterBroadcastReceiver(ConnectionType connectionType) {
		if (mBroadcastReceivers.containsKey(connectionType)) {
			unregisterReceiver(mBroadcastReceivers.get(connectionType));
			
			mBroadcastReceivers.remove(connectionType);
		}
	}
	
	/**
	 * Stops the sync client with the specified connection type.
	 * @param connectionType The connection type of the client to stop.
	 * @param awaitTermination Whether or not to block until the sync
	 * client has terminated.
	 */
	private void stopSyncClient(
			ConnectionType connectionType, boolean awaitTermination) {
		try {
			if (mSyncClients.containsKey(connectionType))
				mSyncClients.get(connectionType)
					.stopSyncClient(awaitTermination);
		} catch (InterruptedException e) { /* Ignore */ }
		
		mSyncClientRunning.remove(connectionType);
		
		unregisterBroadcastReceiver(connectionType);
	}
	
	/**
	 * Stops all the currently running sync clients.
	 * @param awaitTermination Whether or not to block until the sync
	 * clients have terminated.
	 */
	private void stopAllSyncClients(boolean awaitTermination) {
		synchronized (mSyncClients) {
			Iterator<Map.Entry<ConnectionType, P2PSyncClient>> iterator =
					mSyncClients.entrySet().iterator();
			
			while (iterator.hasNext()) {
				stopSyncClient(iterator.next().getKey(), awaitTermination);
				iterator.remove();
			}
		}
	}

	/* (non-Javadoc)
	 * @see org.societies.android.p2p.ISyncService#stopSync(org.societies.android.p2p.ConnectionType, boolean)
	 */
	public void stopSync(ConnectionType connectionType, boolean awaitTermination) {
		stopSyncClient(connectionType, awaitTermination);
		
		if (mSyncClients.isEmpty())
			stopSelf();
	}

	/* (non-Javadoc)
	 * @see org.societies.android.p2p.ISyncService#stopAllSync(boolean)
	 */
	public void stopAllSync(boolean awaitTermination) {
		stopAllSyncClients(awaitTermination);
		
		stopSelf();
	}
	
	/**
	 * Gets whether or not the sync client with the specified connection type
	 * is running.
	 * @param connectionType The connection type of the sync client.
	 * @return <code>true</code> if the sync client is running, otherwise
	 * <code>false</code>.
	 */
	public static boolean isSyncClientRunning(ConnectionType connectionType) {
		return mSyncClientRunning.containsKey(connectionType);
	}
}
