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
import java.util.Collection;
import java.util.UUID;

import org.societies.android.p2p.entity.Request;
import org.societies.android.p2p.entity.Request.RequestType;
import org.societies.android.p2p.entity.Response;
import org.societies.android.platform.entity.Entity;

import android.content.ContentResolver;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

/**
 * A thread sending sync requests to the sync server.
 * 
 * @author Kato
 */
class P2PSyncClient extends Thread {
	
	public static final String TAG = "P2PSyncClient";
	
	/** The interval (in milliseconds) between each poll for updated records. */
	private static final int POLL_INTERVAL = 5000;
	
	private final Context mContext;
	private final P2PConnection mConnection;
	private final P2PConnectionListener mListener;
	private final UpdateReceiver mReceiver;
	private String mUniqueId;
	private boolean mStopping;
	
	/**
	 * Initiates a new sync client.
	 * @param connection The connection to the server, cannot be <code>null</code>.
	 * @param listener The connection listener used for receiving updates
	 * from server, cannot be <code>null</code>.
	 * @param context The context to use, cannot be <code>null</code>.
	 */
	public P2PSyncClient(
			P2PConnection connection,
			P2PConnectionListener listener,
			Context context) {
		mConnection = connection;
		mListener = listener;
		mContext = context;
		
		mReceiver = new UpdateReceiver();
		mStopping = false;
	}
	
	@Override
	public void run() {
		Log.i(TAG, "SyncClient started");
		
		mReceiver.start();
		mUniqueId = getUniqueId();
		
		try {
			while (!mStopping) {
				Collection<Entity> updatedEntities = Entity.getUpdatedEntities(
						mContext.getContentResolver());
				
				sendEntities(updatedEntities);
				
				Thread.sleep(POLL_INTERVAL);
			}
		} catch (IOException e) {
			Log.e(TAG, e.getMessage(), e);
		} catch (InterruptedException e) {
			if (!mStopping)
				Log.e(TAG, "Thread was interrupted while sleeping");
		} catch (Exception e) {
			Log.e(TAG, "Error while fetching entities", e);
		}
		
		waitForReceiverToTerminate();
		
		Log.i(TAG, "SyncClient terminated");
	}

	/**
	 * Sends the specified entities to the server.
	 * @param entities The entities to send.
	 * @throws IOException If an error occurs while sending.
	 */
	private void sendEntities(
			Collection<Entity> entities) throws IOException {
		if (entities.size() > 0) {
			Log.i(TAG, "Sending entities: " + entities.size());
			
			mConnection.connect();
			
			Request request = new Request(mUniqueId, RequestType.UPDATE);
			request.setUpdatedEntities(entities);
			
			try {
				mConnection.write(request);
			} finally {
				mConnection.close();
			}
		}
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
	 * Gets a unique ID that can be used to identify this client. If the
	 * unique ID is not found in the shared preferences, a new one is
	 * generated.
	 * @return A string containing a unique ID.
	 */
	private String getUniqueId() {
		SharedPreferences preferences = mContext.getSharedPreferences(
				P2PConstants.PREFERENCE_FILE, Context.MODE_PRIVATE);
		
		String uniqueId = preferences.getString(
				P2PConstants.PREFERENCE_UNIQUE_ID, null);
		
		if (uniqueId == null) {
			uniqueId = UUID.randomUUID().toString();
			
			preferences.edit()
				.putString(P2PConstants.PREFERENCE_UNIQUE_ID, uniqueId)
				.commit();
		}
		
		return uniqueId;
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
		
		if (awaitTermination && isAlive()) {
			if (getState() == State.TIMED_WAITING)
				interrupt();
			
			join();
		}
	}
	
	/**
	 * Thread used to receive updates pushed from the server.
	 */
	private class UpdateReceiver extends Thread {
		@Override
		public void run() {
			Log.i(TAG, "UpdateReceiver started");
			
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
			
			Log.i(TAG, "UpdateReceiver terminated");
		}
		
		/**
		 * Handles the updates pushed form the server.
		 * @param response The received response entity.
		 */
		private void handleResponse(Response response) {
			if (response == null) {
				Log.i(TAG, "Received response: null");
				return;
			}
			
			Log.i(TAG, "Entities in response: " + response.getEntities().size());
			
			ContentResolver resolver = mContext.getContentResolver();
			
			for (Entity entity : response.getEntities()) {
				entity.fetchLocalIds(resolver);
				
				if (entity.getId() == Entity.ENTITY_DEFAULT_ID)
					entity.insert(resolver);
				else
					entity.update(resolver);
				
				// TODO: Figure out DELETION of entities
			}
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
