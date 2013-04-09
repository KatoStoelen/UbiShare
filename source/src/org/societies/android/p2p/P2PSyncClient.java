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

import org.societies.android.p2p.entity.Response;

import android.content.Context;
import android.util.Log;

/**
 * A thread sending sync requests to the sync server.
 * 
 * @author Kato
 */
class P2PSyncClient extends Thread {
	
	public static final String TAG = "P2PSyncClient";
	
	private final Context mContext;
	private final P2PConnection mConnection;
	private final ConnectionListener mListener;
	private final UpdateReceiver mReceiver;
	private boolean mStopping;
	
	/**
	 * Initiates a new sync client.
	 * @param connection The connection to the server.
	 * @param listener The connection listener used for receiving updates
	 * from server.
	 * @param context The context to use.
	 */
	public P2PSyncClient(
			P2PConnection connection,
			ConnectionListener listener,
			Context context) {
		mConnection = connection;
		mContext = context;
		mListener = listener;
		
		mReceiver = new UpdateReceiver();
		mStopping = false;
	}
	
	@Override
	public void run() {
		mReceiver.start();
		
		// TODO: FIGURE OUT WHETHER OR NOT TO REGISTER A CONTENT OBSERVER
		
		// TODO: ONLY SEND IF ANYTHING IS UPDATED.
		// TODO: SERVER IS TO PUSH UPDATES TO CLIENT.
		
		waitForReceiverToTerminate();
	}

	/**
	 * Waits until the update receiver has terminated.
	 */
	private void waitForReceiverToTerminate() {
		try {
			if (mReceiver.isAlive())
				mReceiver.join();
		} catch (InterruptedException e) { /* Ignore */ }
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
	 * Thread used to receive updates pushed from the server.
	 */
	private class UpdateReceiver extends Thread {
		@Override
		public void run() {
			try {
				while (!mStopping) {
					P2PConnection connection = null;
					try {
						connection = mListener.acceptConnection();
						
						handleResponse(connection.readResponse());
					} catch (InterruptedIOException e) {
						/* Ignore */
					} finally {
						closeConnection(connection);
					}
				}
			} catch (IOException e) {
				Log.e(TAG, e.getMessage(), e);
			} finally {
				closeListener();
			}
		}
		
		/**
		 * Handles the updates pushed form the server.
		 * @param response The received response entity.
		 */
		private void handleResponse(Response response) {
			// TODO IMPLEMENT
		}

		/**
		 * Closes the P2P connection listener.
		 */
		private void closeListener() {
			if (mListener != null) {
				try {
					mListener.close();
				} catch (IOException e) { /* Ignore */ }
			}
		}

		/**
		 * Closes the specified P2P connection.
		 * @param connection The P2P connection to close.
		 */
		private void closeConnection(P2PConnection connection) {
			if (connection != null) {
				try {
					connection.close();
				} catch (IOException e) { /* Ignore */ }
			}
		}
	}
}
