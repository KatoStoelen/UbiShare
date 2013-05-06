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

import org.societies.android.p2p.HandshakeLock.LockType;
import org.societies.android.p2p.P2PConnection.ConnectionType;
import org.societies.android.p2p.UpdatePoller.UpdateListener;
import org.societies.android.p2p.entity.Request;
import org.societies.android.p2p.entity.Request.RequestType;
import org.societies.android.p2p.entity.Response;
import org.societies.android.platform.entity.Entity;

import android.content.ContentResolver;
import android.content.Context;
import android.util.Log;

/**
 * A thread accepting connections and handling sync requests from
 * clients.
 * 
 * @author Kato
 */
class P2PSyncServer extends Thread implements UpdateListener {
	
	public static final String TAG = "P2PSyncServer";
	
	private final P2PConnectionListener mListener;
	private final Context mContext;
	private final HandshakeLock mHandshakeLock;
	private final UpdatePoller mPoller;
	private PeerList mPeers;
	private boolean mStopping;
	
	/**
	 * Initializes a new sync server.
	 * @param context The context to use, cannot be <code>null</code>.
	 * @param listener The connection listener, cannot be <code>null</code>.
	 */
	public P2PSyncServer(Context context, P2PConnectionListener listener) {
		mContext = context;
		mListener = listener;
		
		mHandshakeLock = new HandshakeLock();
		mPoller = new UpdatePoller(context, this);
		mPeers = new PeerList();
		mStopping = false;
		
		// TODO: FIGURE OUT THE ACCOUNT NAME AND TYPE
		Entity.SELECTION_ACCOUNT_NAME = "p2p";
		Entity.SELECTION_ACCOUNT_TYPE = "p2p";
	}

	@Override
	public void run() {
		Log.i(TAG, "P2PSyncServer started");
		
		mPoller.start();
		
		try {
			mListener.initialize();
			
			int heartBeatCounter = 0;
			while (!mStopping) {
				try {
					P2PConnection connection = mListener.acceptConnection();
					
					if (connection != null)
						new ClientHandler(connection).start();
					else
						Log.e(TAG, "Accepted connection: null");
				} catch (InterruptedIOException e) {
					heartBeatCounter++;
					
					if (heartBeatCounter == 10) {
						heartBeatCounter = 0;
						
						Log.i(TAG, "Alive");
					}
				}
			}
		} catch (IOException e) {
			Log.e(TAG, e.getMessage(), e);
		} finally {
			try {
				if (mListener != null)
					mListener.close();
			} catch (IOException e) { /* Ignore */ }
		}
		
		Log.i(TAG, "P2PSyncServer terminated");
	}
	
	/* (non-Javadoc)
	 * @see org.societies.android.p2p.UpdatePoller.UpdateListener#onEntitiesAvailable(java.util.Collection)
	 */
	public void onEntitiesAvailable(Collection<Entity> entities) {
		Response response = new Response();
		response.setEntities(entities);
		
		synchronized (mPeers) {
			for (Peer peer : mPeers) {
				if (peer.isActive())
					new UpdateSender(peer, response).start();
			}
		}
		
		mPoller.resetEntityDirtyFlag(entities);
	}
	
	/**
	 * Stops the sync server.
	 * @param awaitTermination Whether or not to block until the server
	 * has terminated.
	 * @throws InterruptedException If awaiting termination and thread is
	 * interrupted.
	 */
	public void stopServer(boolean awaitTermination)
			throws InterruptedException {
		mStopping = true;
		
		mPoller.stopPolling();
		
		if (awaitTermination && isAlive())
			join();
	}
	
	/**
	 * Gets the peer with the specified unique ID.
	 * @param uniqueId The unique ID of the peer.
	 * @return The peer with the specified unique ID, or <code>null</code> if
	 * it does not exist.
	 */
	private Peer getPeer(String uniqueId) {
		synchronized (mPeers) {
			return mPeers.getPeer(uniqueId);
		}
	}
	
	/**
	 * Adds a peer to the list.
	 * @param peer The peer to add.
	 */
	private void addPeer(Peer peer) {
		synchronized (mPeers) {
			mPeers.add(peer);
		}
	}
	
	/**
	 * Thread responsible for sending updates to the clients.
	 */
	private class ClientHandler extends Thread {
		
		public static final String TAG = P2PSyncServer.TAG + ":ClientHandler";
		
		private P2PConnection mConnection;
		
		/**
		 * Initializes a new client handler.
		 * @param connection The connection to the client.
		 */
		public ClientHandler(P2PConnection connection) {
			mConnection = connection;
		}
		
		@Override
		public void run() {
			Log.i(TAG, "ClientHandler started");
			
			try {
				Request request = mConnection.receiveRequest();
				
				Log.i(TAG, "Handling client: " + request.getUniqueId());
				
				if (request.getType() == RequestType.HANDSHAKE)
					handleHandshake(request);
				else if (request.getType() == RequestType.UPDATE)
					handleClientUpdates(request);
				else
					Log.e(TAG, "Received request of unknown type: " +
							request.getType());
			} catch (InterruptedIOException e) {
				Log.e(TAG, "Timeout while reading request", e);
			} catch (InterruptedException e) {
				Log.i(TAG, "Interrupted while waiting for lock");
			} catch (Exception e) {
				Log.e(TAG, e.getMessage(), e);
			} finally {
				try {
					mConnection.close();
				} catch (IOException e) { /* Ignore */ }
			}
			
			Log.i(TAG, "ClientHandler terminated");
		}
		
		/**
		 * Handles handshake requests.
		 * @param request The handshake request.
		 * @throws Exception If an error occurs while handling request.
		 */
		private void handleHandshake(Request request) throws Exception {
			Log.i(TAG, "Handshake: " + request.getUniqueId());
			
			mHandshakeLock.lock(LockType.HANDSHAKE);
			
			Peer peer = null;
			if ((peer = getPeer(request.getUniqueId())) == null) {
				peer = getInitializedPeer(request.getUniqueId());
				addPeer(peer);
			}
			peer.setActive(true);
			
			// TODO: find a way to update address if client has been offline (WiFi direct)
			
			sendEntities(Entity.getAllEntities(mContext.getContentResolver()));
			
			mHandshakeLock.unlock(LockType.HANDSHAKE);
		}
		
		/**
		 * Sends the specified entities to the client.
		 * @param entities The entities to send.
		 * @throws IOException If an error occurs while sending entities.
		 */
		private void sendEntities(Collection<Entity> entities) throws IOException {
			Response response = new Response();
			response.setEntities(entities);
			
			mConnection.send(response);
		}

		/**
		 * Handles client update requests.
		 * @param request The client update request.
		 * @throws InterruptedException If the thread is interrupted while acquiring
		 * the lock.
		 */
		private void handleClientUpdates(Request request) throws InterruptedException {
			Log.i(TAG, "Received updates: " + request.getUpdatedEntities().size());
			
			mHandshakeLock.lock(LockType.UPDATE);
			
			Collection<Entity> update = request.getUpdatedEntities();
			processUpdate(update);
			
			Response updateResponse = new Response();
			updateResponse.setEntities(update);
			
			synchronized (mPeers) {
				for (Peer peer : mPeers) {
					if (peer.isActive() &&
							!peer.getUniqueId().equals(request.getUniqueId()))
						new UpdateSender(peer, updateResponse).start();
				}
			}
			
			mHandshakeLock.unlock(LockType.UPDATE);
		}
		
		/**
		 * Processes the received update by generating global IDs, if needed, and
		 * inserting them into the database.
		 * @param update The received update.
		 */
		private void processUpdate(Collection<Entity> update) {
			ContentResolver resolver = mContext.getContentResolver();
			
			for (Entity entity : update) {
				entity.fetchLocalIds(resolver);
				
				if (entity.getDeletedFlag() != 0
						&& entity.getId() != Entity.ENTITY_DEFAULT_ID)
					entity.delete(resolver);
				else if (entity.getDeletedFlag() != 0)
					Log.e(TAG, "Could not delete entity: id = -1");
				else if (entity.getId() == Entity.ENTITY_DEFAULT_ID)
					entity.insert(resolver);
				else
					entity.update(resolver);
			}
		}
		
		/**
		 * Initializes a new peer with the specified unique ID.
		 * @param uniqueId The unique ID of the peer.
		 * @return An initialized <code>Peer</code> instance.
		 */
		private Peer getInitializedPeer(String uniqueId) {
			if (mConnection.getConnectionType() == ConnectionType.WIFI_DIRECT) {
				WiFiDirectConnection connection = (WiFiDirectConnection) mConnection;
				
				return new WiFiDirectPeer(
						uniqueId,
						connection.getRemoteIp(),
						P2PConstants.WIFI_DIRECT_CLIENT_PORT);
			} else if (mConnection.getConnectionType() == ConnectionType.BLUETOOTH) {
				BluetoothConnection connection = (BluetoothConnection) mConnection;
				
				return new BluetoothPeer(uniqueId, connection.getRemoteDevice());
			} else {
				throw new IllegalStateException("Unknown connection type");
			}
		}
	}
	
	/**
	 * Thread used for sending updates to clients.
	 */
	private class UpdateSender extends Thread {
		
		public static final String TAG = P2PSyncServer.TAG + ":UpdateSender";
		
		private Peer mPeer;
		private Response mResponse;
		
		/**
		 * Initializes a new update sender thread.
		 * @param peer The peer to send to.
		 * @param response The response to send.
		 */
		public UpdateSender(Peer peer, Response response) {
			mPeer = peer;
			mResponse = response;
		}
		
		@Override
		public void run() {
			P2PConnection connection = null;
			
			try {
				mHandshakeLock.lock(LockType.UPDATE);
				
				connection = mPeer.connect();
				connection.send(mResponse);
				
				mPeer.setLastUpdateTimeNow();
				
				mHandshakeLock.unlock(LockType.UPDATE);
			} catch (InterruptedIOException e) {
				Log.e(TAG, "Timeout when connecting to client");
				mPeer.setActive(false);
			} catch (IOException e) {
				Log.e(TAG, e.getMessage(), e);
				mPeer.setActive(false);
			} catch (InterruptedException e) {
				Log.e(TAG, "Interrupted while waiting for lock");
			} finally {
				if (connection != null) {
					try {
						connection.close();
					} catch (IOException e) { /* Ignore */ }
				}
			}
		}
	}
}
