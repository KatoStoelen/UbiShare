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
import org.societies.android.p2p.net.P2PConnectionListener;

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
	
	public static final Map<ConnectionType, Integer> mSyncServerRunning =
			Collections.synchronizedMap(new HashMap<ConnectionType, Integer>());
	
	private final IBinder mBinder = new LocalServiceBinder(this);
	private Map<ConnectionType, P2PSyncServer> mSyncServers =
			Collections.synchronizedMap(new HashMap<ConnectionType, P2PSyncServer>());
	private final Map<ConnectionType, ServiceBroadcastReceiver> mBroadcastReceivers =
			Collections.synchronizedMap(new HashMap<ConnectionType, ServiceBroadcastReceiver>());
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		if (intent != null) {
			P2PConnectionListener listener = (P2PConnectionListener)
					intent.getParcelableExtra(EXTRA_CONNECTION_LISTENER);
			
			if (!mSyncServers.containsKey(listener.getConnectionType())) {
				P2PSyncServer syncServer = new P2PSyncServer(this, listener);
				
				startSyncServer(syncServer, listener.getConnectionType());
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
	 * Starts the specified sync server.
	 * @param syncServer The sync server to start.
	 * @param connectionType The connection type in use.
	 */
	private void startSyncServer(
			P2PSyncServer syncServer, ConnectionType connectionType) {
		syncServer.start();
		
		mSyncServers.put(connectionType, syncServer);
		mSyncServerRunning.put(connectionType, 1);
		
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
	 * Stops the sync server with the specified connection type.
	 * @param connectionType The connection type of the server to stop.
	 * @param awaitTermination Whether or not to block until the sync
	 * server has terminated.
	 */
	private void stopSyncServer(
			ConnectionType connectionType, boolean awaitTermination) {
		try {
			if (mSyncServers.containsKey(connectionType))
				mSyncServers.get(connectionType).stopServer(awaitTermination);
		} catch (InterruptedException e) { /* Ignore */ }
		
		mSyncServerRunning.remove(connectionType);
		
		unregisterBroadcastReceiver(connectionType);
	}
	
	/**
	 * Stops all the currently running sync servers.
	 * @param awaitTermination Whether or not to block until the sync
	 * servers have terminated.
	 */
	private void stopAllSyncServers(boolean awaitTermination) {
		synchronized (mSyncServers) {
			Iterator<Map.Entry<ConnectionType, P2PSyncServer>> iterator =
					mSyncServers.entrySet().iterator();
			
			while (iterator.hasNext()) {
				stopSyncServer(iterator.next().getKey(), awaitTermination);
				iterator.remove();
			}
		}
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
