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

import android.content.Context;

/**
 * A thread sending sync requests to the sync server.
 * 
 * @author Kato
 */
class P2PSyncClient extends Thread {
	
	/**
	 * The number of milliseconds between each sync.
	 */
	public static final int DEFAULT_SYNC_INTERVAL = 5000;

	private Context mContext;
	private P2PConnection mConnection;
	private int mSyncInterval;
	private boolean mStopping;
	
	/**
	 * Initiates a new sync client.
	 * @param connection The connection to the server.
	 * @param syncInterval The number of milliseconds between each sync.
	 * @param context The context to use.
	 */
	public P2PSyncClient(
			P2PConnection connection, int syncInterval, Context context) {
		mConnection = connection;
		mContext = context;
		setSyncInterval(syncInterval);
		
		mStopping = false;
	}
	
	@Override
	public void run() {
		// TODO: IMPLEMENT
	}
	
	/**
	 * Stops the sync client.
	 * @param awaitTermination Whether or not to block until sync client
	 * has terminated.
	 * @throws InterruptedException If awaiting termination and thread is
	 * interrupted.
	 */
	public void stopSyncClient(
			boolean awaitTermination) throws InterruptedException {
		mStopping = true;
		
		if (awaitTermination && isAlive())
			join();
	}

	/**
	 * Gets the current sync interval.
	 * @return The current sync interval.
	 */
	public int getSyncInterval() {
		return mSyncInterval;
	}

	/**
	 * Sets the current sync interval.
	 * @param syncInterval The number of milliseconds between each sync.
	 */
	public void setSyncInterval(int syncInterval) {
		mSyncInterval = syncInterval;
	}
}
