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

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

/**
 * Service wrapper of the sync server.
 * 
 * @author Kato
 */
class P2PSyncServerService extends Service implements ISyncService {
	
	/** The connection listener. */
	public static final String EXTRA_CONNECTION_LISTENER = "connection_listener";
	
	/** Whether or not the service is running. */
	public static boolean IS_RUNNING = false;
	
	private final IBinder mBinder = new LocalServiceBinder(this);
	private P2PSyncServer mSyncServer;
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		P2PConnectionListener listener =
				(P2PConnectionListener) intent.getSerializableExtra(
						EXTRA_CONNECTION_LISTENER);
		
		if (mSyncServer == null) {
			mSyncServer = new P2PSyncServer(this, listener);
			mSyncServer.start();
		}
		
		IS_RUNNING = true;
		
		return START_STICKY;
	}

	@Override
	public IBinder onBind(Intent intent) {
		return mBinder;
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		
		stopSync(true);
		
		IS_RUNNING = false;
	}

	/*
	 * (non-Javadoc)
	 * @see org.societies.android.p2p.ISyncService#stopSync(boolean)
	 */
	public void stopSync(boolean awaitTermination) {
		try {
			if (mSyncServer != null)
				mSyncServer.stopServer(awaitTermination);
		} catch (InterruptedException e) { /* Ignore */ }
	}
}
