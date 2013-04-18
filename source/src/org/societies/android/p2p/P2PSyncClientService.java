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
 * Service wrapper of the sync client.
 * 
 * @author Kato
 */
class P2PSyncClientService extends Service {
	
	/** Name of the P2P connection extra. */
	public static final String EXTRA_CONNECTION = "extra_connection";
	/** Name of the connection listener extra. */
	public static final String EXTRA_LISTENER = "extra_listener";
	/** Name of the unique ID extra. */
	public static final String EXTRA_UNIQUE_ID = "extra_unique_id";
	
	private final IBinder mBinder = new SyncClientBinder();
	private P2PSyncClient mSyncClient;
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		P2PConnection connection =
				(P2PConnection) intent.getSerializableExtra(EXTRA_CONNECTION);
		P2PConnectionListener listener =
				(P2PConnectionListener) intent.getSerializableExtra(EXTRA_LISTENER);
		String uniqueId = intent.getStringExtra(EXTRA_UNIQUE_ID);
		
		if (mSyncClient == null) {
			mSyncClient = new P2PSyncClient(uniqueId, connection, listener, this);
			mSyncClient.start();
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
		
		stopSyncClient(true);
	}
	
	/**
	 * Stops the sync client.
	 * @param awaitTermination Whether or not to block until sync client
	 * has terminated.
	 */
	public void stopSyncClient(boolean awaitTermination) {
		try {
			if (mSyncClient != null)
				mSyncClient.stopSyncClient(awaitTermination);
		} catch (InterruptedException e) { /* Ignore */ }
	}
	
	/**
	 * Custom binder used to obtain service object reference.
	 */
	public class SyncClientBinder extends Binder {
		/**
		 * Gets the service object.
		 * @return The service object.
		 */
		public P2PSyncClientService getService() {
			return P2PSyncClientService.this;
		}
	}
}
