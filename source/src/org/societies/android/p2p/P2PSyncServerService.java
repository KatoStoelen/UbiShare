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
 * Service wrapper of the sync server.
 * 
 * @author Kato
 */
public class P2PSyncServerService extends Service implements ISyncService {
	
	/** The connection listener. */
	public static final String EXTRA_CONNECTION_LISTENER = "connection_listener";
	
	public static final Map<ConnectionType, String> mSyncServerRunning =
			new HashMap<ConnectionType, String>();
	
	private final IBinder mBinder = new LocalServiceBinder(this);
	private Map<ConnectionType, P2PSyncServer> mSyncServers =
			new HashMap<ConnectionType, P2PSyncServer>();
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		if (intent != null) {
			P2PConnectionListener listener = (P2PConnectionListener)
					intent.getParcelableExtra(EXTRA_CONNECTION_LISTENER);
			
			if (!mSyncServers.containsKey(listener.getConnectionType())) {
				P2PSyncServer syncServer = new P2PSyncServer(this, listener);
				syncServer.start();
				
				mSyncServers.put(listener.getConnectionType(), syncServer);
				mSyncServerRunning.put(listener.getConnectionType(), "true");
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
		
		stopAllSyncServers(false);
	}

	/**
	 * Stops the sync server with the specified connection type.
	 * @param connectionType The connection type of the server to stop.
	 * @param awaitTermination Whether or not to block until the sync
	 * server has terminated.
	 */
	private void stopSyncServer(
			ConnectionType connectionType, boolean awaitTermination) {
		try {
			if (mSyncServers.containsKey(connectionType))
				mSyncServers.get(connectionType)
					.stopServer(awaitTermination);
		} catch (InterruptedException e) { /* Ignore */ }
		
		mSyncServers.remove(connectionType);
		mSyncServerRunning.remove(connectionType);
	}
	
	/**
	 * Stops all the currently running sync servers.
	 * @param awaitTermination Whether or not to block until the sync
	 * servers have terminated.
	 */
	private void stopAllSyncServers(boolean awaitTermination) {
		for (ConnectionType connectionType : mSyncServers.keySet())
			stopSyncServer(connectionType, awaitTermination);
	}

	/* (non-Javadoc)
	 * @see org.societies.android.p2p.ISyncService#stopSync(org.societies.android.p2p.ConnectionType, boolean)
	 */
	public void stopSync(ConnectionType connectionType, boolean awaitTermination) {
		stopSyncServer(connectionType, awaitTermination);
		
		if (mSyncServers.isEmpty())
			stopSelf();
	}

	/* (non-Javadoc)
	 * @see org.societies.android.p2p.ISyncService#stopAllSync(boolean)
	 */
	public void stopAllSync(boolean awaitTermination) {
		stopAllSyncServers(awaitTermination);
		
		stopSelf();
	}
	
	/**
	 * Gets whether or not the sync server with the specified connection type is
	 * running.
	 * @param connectionType The connection type of the sync server.
	 * @return <code>true</code> if the sync server is running, otherwise
	 * <code>false</code>.
	 */
	public static boolean isSyncServerRunning(ConnectionType connectionType) {
		return mSyncServerRunning.containsKey(connectionType);
	}
}
