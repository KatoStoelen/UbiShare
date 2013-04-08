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
import android.os.Binder;
import android.os.IBinder;

/**
 * Service wrapper of the sync server.
 * 
 * @author Kato
 */
public class P2PSyncServerService extends Service {
	
	/** The connection listener. */
	public static final String EXTRA_CONNECTION_LISTENER = "connection_listener";
	
	private final IBinder mBinder = new SyncServerBinder();
	private P2PSyncServer mSyncServer;
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		IConnectionListener listener = 
				(IConnectionListener) intent.getSerializableExtra(
						EXTRA_CONNECTION_LISTENER);
		
		if (mSyncServer == null) {
			mSyncServer = new P2PSyncServer(this, listener);
			mSyncServer.start();
		}
		
		return START_STICKY;
	}

	@Override
	public IBinder onBind(Intent intent) {
		return mBinder;
	}
	
	/**
	 * Custom binder used to obtain service object reference.
	 */
	public class SyncServerBinder extends Binder {
		/**
		 * Gets the service object.
		 * @return The service object.
		 */
		public P2PSyncServerService getService() {
			return P2PSyncServerService.this;
		}
	}
}
