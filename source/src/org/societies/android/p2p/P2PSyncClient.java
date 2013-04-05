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

import java.io.IOException;
import java.io.InterruptedIOException;

import android.content.Context;
import android.util.Log;

/**
 * A thread sending sync requests to the sync server.
 * 
 * @author Kato
 */
class P2PSyncClient extends Thread {
	
	public static final String TAG = "P2PSyncClient";
	
	/**
	 * The number of milliseconds between each sync.
	 */
	public static final int DEFAULT_SYNC_INTERVAL = 5000;

	private final Object SYNC_LOCK = new Object();
	
	private Context mContext;
	private P2PConnection mConnection;
	private int mSyncInterval;
	private boolean mStopping;
	private boolean mSyncNow;
	
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
		mSyncNow = false;
	}
	
	@Override
	public void run() {
		try {
			while (!mStopping) {
				mSyncNow = false;
				
				try {
					if (!mConnection.connect()) {
						throw new IOException("Connection not established");
					} else {
						// TODO: ONLY SEND IF ANYTHING IS UPDATED.
						// TODO: SERVER IS TO PUSH UPDATES TO CLIENT.
					}
				} finally {
					mConnection.close();
				}
				
				waitForNextSync();
			}
		} catch (InterruptedIOException e) {
			Log.e(TAG, "Failed to connect to server: timeout");
		} catch (IOException e) {
			Log.e(TAG, e.getMessage(), e);
		} catch (InterruptedException e) {
			Log.e(TAG, "Interrupted while waiting");
		}
	}
	
	/**
	 * Waits the number of milliseconds specified by the sync interval.
	 * @throws InterruptedException If the thread is interrupted while
	 * waiting.
	 */
	private void waitForNextSync() throws InterruptedException {
		if (!mStopping && !mSyncNow) {
			synchronized (SYNC_LOCK) {
				SYNC_LOCK.wait(mSyncInterval);
			}
		}
	}
	
	/**
	 * Triggers an immediate synchronization.
	 */
	public void syncNow() {
		mSyncNow = true;
		
		synchronized (SYNC_LOCK) {
			SYNC_LOCK.notify();
		}
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
