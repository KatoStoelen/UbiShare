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
import java.util.LinkedList;
import java.util.Queue;

import org.societies.android.p2p.UpdatePoller.UpdateListener;
import org.societies.android.p2p.entity.Request;
import org.societies.android.p2p.entity.Request.RequestType;
import org.societies.android.p2p.entity.Response;
import org.societies.android.p2p.net.P2PConnection;
import org.societies.android.p2p.net.P2PConnectionListener;
import org.societies.android.platform.entity.Entity;

import android.content.ContentResolver;
import android.content.Context;
import android.util.Log;

/**
 * A thread sending sync requests to the sync server.
 * 
 * @author Kato
 */
class P2PSyncClient extends Thread implements UpdateListener {
	
	public static final String TAG = "P2PSyncClient";
	
	private final ContentResolver mResolver;
	private final P2PConnection mConnection;
	private final P2PConnectionListener mListener;
	private final UpdateReceiver mReceiver;
	private final UpdatePoller mPoller;
	private final String mUniqueId;
	private Queue<Collection<Entity>> mUpdateQueue;
	private boolean mStopping;
	
	/**
	 * Initiates a new sync client.
	 * @param uniqueId The unique ID of this client.
	 * @param connection The connection to the server, cannot be <code>null</code>.
	 * @param listener The connection listener used for receiving updates
	 * from server, cannot be <code>null</code>.
	 * @param context The context to use, cannot be <code>null</code>.
	 */
	public P2PSyncClient(
			String uniqueId,
			P2PConnection connection,
			P2PConnectionListener listener,
			Context context) {
		mUniqueId = uniqueId;
		mConnection = connection;
		mListener = listener;
		mResolver = context.getContentResolver();
		
		mReceiver = new UpdateReceiver();
		mPoller = new UpdatePoller(context, this);
		mUpdateQueue = new LinkedList<Collection<Entity>>();
		mStopping = false;
		
		// TODO: FIGURE OUT THE ACCOUNT NAME AND TYPE
		Entity.SELECTION_ACCOUNT_NAME = "p2p";
		Entity.SELECTION_ACCOUNT_TYPE = "p2p";
	}
	
	@Override
	public void run() {
		Log.i(TAG, "SyncClient started");
		
		mReceiver.start();
		
		try {
			performHandshake();
			
			mPoller.start();
			
			while (!mStopping) {
				Collection<Entity> updatedEntities = null;
				synchronized (mUpdateQueue) {
					while (mUpdateQueue.isEmpty())
						mUpdateQueue.wait();
					
					updatedEntities = mUpdateQueue.poll();
				}
				
				sendEntities(updatedEntities);
			}
		} catch (InterruptedException e) {
			if (!mStopping)
				Log.e(TAG, "Interrupted while waiting for queue");
		} catch (Exception e) {
			Log.e(TAG, e.getMessage(), e);
		} finally {
			mStopping = true;
		}
		
		Log.i(TAG, "Waiting for UpdateReceiver to terminate...");
		waitForReceiverToTerminate();
		
		Log.i(TAG, "SyncClient terminated");
	}
	
	/* (non-Javadoc)
	 * @see org.societies.android.p2p.UpdatePoller.UpdateListener#onEntitiesAvailable(java.util.Collection)
	 */
	public void onEntitiesAvailable(Collection<Entity> entities) {
		Log.i(TAG, "Updated Entities Available: " + entities.size());
		
		synchronized (mUpdateQueue) {
			mUpdateQueue.add(entities);
			mUpdateQueue.notify();
		}
	}

	/**
	 * Sends a handshake request to the server.
	 * @throws IOException If an error occurs while sending request.
	 * @throws InterruptedIOException It a timeout occurs while sending request.
	 */
	private void performHandshake() throws InterruptedIOException, IOException {
		Log.i(TAG, "Performing handshake...");
		
		Request handshake = new Request(mUniqueId, RequestType.HANDSHAKE);
		
		mConnection.connect();
		
		try {
			mConnection.send(handshake);
			
			Response response = mConnection.receiveResponse();
			insertEntities(response.getEntities());
		} finally {
			mConnection.close();
		}
		
	}

	/**
	 * Inserts the specified entities into the database.
	 * @param entities The entities to insert.
	 */
	private void insertEntities(Collection<Entity> entities) {
		Log.i(TAG, "Inserting entities: " + entities.size());
		
		for (Entity entity : entities) {
			entity.fetchLocalIds(mResolver);
			
			if (entity.getId() == Entity.ENTITY_DEFAULT_ID)
				entity.insert(mResolver);
			else
				entity.update(mResolver);
		}
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
			
			Log.i(TAG , "Connected to server: " + mConnection.connect());
			
			Request request = new Request(mUniqueId, RequestType.UPDATE);
			request.setUpdatedEntities(entities);
			
			try {
				mConnection.send(request);
				
				mPoller.resetEntityDirtyFlag(entities);
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
	 * Stops the sync client.
	 * @param awaitTermination Whether or not to block until sync client
	 * has terminated.
	 * @throws InterruptedException If awaiting termination and thread is
	 * interrupted.
	 */
	public void stopSyncClient(boolean awaitTermination)
			throws InterruptedException {
		mStopping = true;
		
		mPoller.stopPolling();
		
		if (getState() == State.WAITING)
			interrupt();
		
		if (awaitTermination && isAlive())
			join();
	}
	
	/**
	 * Thread used to receive updates pushed from the server.
	 */
	private class UpdateReceiver extends Thread {
		
		public static final String TAG = P2PSyncClient.TAG + ":UpdateReceiver";
		
		private final Queue<Response> mResponseQueue =
				new LinkedList<Response>();
		private final ResponseHandler mHandler = new ResponseHandler();
		
		@Override
		public void run() {
			Log.i(TAG, "UpdateReceiver started");
			
			mHandler.start();
			
			try {
				mListener.initialize();
				
				while (!mStopping) {
					P2PConnection connection = null;
					try {
						connection = mListener.acceptConnection();
						
						enqueueResponse(connection.receiveResponse());
					} catch (InterruptedIOException e) {
						/* Ignore */
					} finally {
						closeConnection(connection);
					}
				}
			} catch (Exception e) {
				Log.e(TAG, e.getMessage(), e);
			} finally {
				closeListener();
			}
			
			if (!mStopping || mHandler.getState() == State.TIMED_WAITING)
				mHandler.interrupt();
			
			Log.i(TAG, "UpdateReceiver terminated");
		}
		
		/**
		 * Enqueues the specified response.
		 * @param response The response to enqueue.
		 */
		private void enqueueResponse(Response response) {
			synchronized (mResponseQueue) {
				mResponseQueue.add(response);
				mResponseQueue.notify();
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
		
		/**
		 * Handle responses in separate thread to make sure that the update receiver
		 * is always ready to receive updates.
		 */
		private class ResponseHandler extends Thread {
			
			public static final String TAG = UpdateReceiver.TAG + ":ResponseHandler";
			
			@Override
			public void run() {
				Log.i(TAG, "ResponseHandler started");
				
				try {
					while (!mStopping) {
						Response response = null;
						synchronized (mResponseQueue) {
							while (mResponseQueue.isEmpty())
								mResponseQueue.wait();
							
							response = mResponseQueue.poll();
						}
						
						handleResponse(response);
					}
				} catch (InterruptedException e) {
					if (!mStopping)
						Log.e(TAG, "Interrupted while waiting for queue");
				} catch (Exception e) {
					Log.e(TAG, e.getMessage(), e);
				}
				
				Log.i(TAG, "ResponseHandler terminated");
			}
			
			/**
			 * Handles the received updates.
			 * @param response The received response.
			 */
			private void handleResponse(Response response) {
				if (response == null) {
					Log.i(TAG, "Received response: null");
					return;
				}
				
				Log.i(TAG, "Entities in response: " + response.getEntities().size());
				
				for (Entity entity : response.getEntities()) {
					entity.fetchLocalIds(mResolver);
					
					if (entity.getDeletedFlag() != 0
							&& entity.getId() != Entity.ENTITY_DEFAULT_ID)
						entity.delete(mResolver);
					else if (entity.getDeletedFlag() != 0)
						Log.e(TAG, "Could not delete entity: id = -1");
					else if (entity.getId() == Entity.ENTITY_DEFAULT_ID)
						entity.insert(mResolver);
					else
						entity.update(mResolver);
				}
			}
		}
	}
}
