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

import java.util.HashMap;
import java.util.Map;

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
	
	private static final Map<ConnectionType, String> mSyncClientRunning =
			new HashMap<ConnectionType, String>();
	
	private final IBinder mBinder = new LocalServiceBinder(this);
	private Map<ConnectionType, P2PSyncClient> mSyncClients =
			new HashMap<ConnectionType, P2PSyncClient>();
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		if (intent != null) {
			P2PConnection connection = (P2PConnection)
					intent.getParcelableExtra(EXTRA_CONNECTION);
			P2PConnectionListener listener = (P2PConnectionListener)
					intent.getParcelableExtra(EXTRA_LISTENER);
			String uniqueId = intent.getStringExtra(EXTRA_UNIQUE_ID);
			
			if (!mSyncClients.containsKey(connection.getConnectionType())) {
				P2PSyncClient syncClient = new P2PSyncClient(
						uniqueId, connection, listener, this);
				syncClient.start();
				
				mSyncClients.put(connection.getConnectionType(), syncClient);
				mSyncClientRunning.put(connection.getConnectionType(), "true");
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
		
		mSyncClients.remove(connectionType);
		mSyncClientRunning.remove(connectionType);
	}
	
	/**
	 * Stops all the currently running sync clients.
	 * @param awaitTermination Whether or not to block until the sync
	 * clients have terminated.
	 */
	private void stopAllSyncClients(boolean awaitTermination) {
		for (ConnectionType connectionType : mSyncClients.keySet())
			stopSyncClient(connectionType, awaitTermination);
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
