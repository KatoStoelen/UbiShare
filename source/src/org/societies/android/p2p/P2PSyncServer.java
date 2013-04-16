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

import org.societies.android.p2p.P2PConnection.ConnectionType;
import org.societies.android.p2p.entity.Request;
import org.societies.android.p2p.entity.Request.RequestType;
import org.societies.android.p2p.entity.Response;
import org.societies.android.platform.entity.Entity;

import android.content.Context;
import android.util.Log;

/**
 * A thread accepting connections and handling sync requests from
 * clients.
 * 
 * @author Kato
 */
class P2PSyncServer extends Thread {
	
	public static final String TAG = "P2PSyncServer";
	
	private final P2PConnectionListener mListener;
	private final Context mContext;
	private final HandshakeLock mHandshakeLock;
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
		mPeers = new PeerList();
		mStopping = false;
	}

	@Override
	public void run() {
		try {
			while (!mStopping) {
				try {
					P2PConnection connection = mListener.acceptConnection();
					
					if (connection != null)
						new ClientHandler(connection).start();
				} catch (InterruptedIOException e) { /* Ignore */ }
			}
		} catch (IOException e) {
			Log.e(TAG, e.getMessage(), e);
		}
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
		
		if (awaitTermination && isAlive())
			join();
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
			try {
				Request request = mConnection.readRequest();
				
				if (request.getType() == RequestType.HANDSHAKE)
					handleHandshake(request);
				else if (request.getType() == RequestType.UPDATE)
					handleClientUpdates(request);
				else
					Log.e(TAG, "Received request of unknown type: " +
							request.getType());
			} catch (InterruptedIOException e) {
				Log.e(TAG, "Timeout while reading request");
			} catch (IOException e) {
				Log.e(TAG, e.getMessage(), e);
			} catch (InterruptedException e) {
				Log.i(TAG, "Interrupted");
			} finally {
				try {
					mConnection.close();
				} catch (IOException e) { /* Ignore */ }
			}
		}
		
		/**
		 * Handles handshake requests.
		 * @param request The handshake request.
		 * @throws InterruptedException If the thread is interrupted while acquiring
		 * the lock.
		 * @throws IOException If an error occurs while sending response.
		 */
		private void handleHandshake(Request request)
				throws InterruptedException, IOException {
			mHandshakeLock.lock(true);
			
			Peer peer = null;
			if ((peer = getPeer(request.getUniqueId())) == null) {
				peer = getInitializedPeer(request.getUniqueId());
				addPeer(peer);
			}
			peer.setActive(true);
			
			sendAllEntities();
			
			mHandshakeLock.unlock(true);
		}

		/**
		 * Sends all the entities to the client.
		 */
		private void sendAllEntities() throws IOException {
			Response response = new Response();
			//response.setEntities(entities); TODO: Get all entities.
			
			mConnection.write(response);
		}

		/**
		 * Handles client update requests.
		 * @param request The client update request.
		 * @throws InterruptedException If the thread is interrupted while acquiring
		 * the lock.
		 */
		private void handleClientUpdates(Request request) throws InterruptedException {
			mHandshakeLock.lock(false);
			
			// TODO: IMPLEMENT
			
			mHandshakeLock.unlock(false);
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
				return null;
			} else {
				return null;
			}
		}
	}
	
	/**
	 * Lock ensuring that the server does not send updates while a handshake is
	 * in progress. Multiple handshakes can be handled simultaneously, the same
	 * goes for updates. The only restriction is that updates and handshakes does
	 * not happen at the same time.
	 * 
	 * @author Kato
	 */
	private class HandshakeLock {
		
		private final Object mLock = new Object();
		private int mUpdateLockCount = 0;
		private int mHandshakeLockCount = 0;
		
		/**
		 * Acquires the lock.
		 * @param handshake Whether or not it is a handshake lock.
		 * @throws InterruptedException If the thread is interrupted while waiting
		 * for lock.
		 */
		public void lock(boolean handshake) throws InterruptedException {
			synchronized (mLock) {
				if (handshake) {
					while (mUpdateLockCount > 0)
						mLock.wait();

					mHandshakeLockCount++;
				} else {
					while (mHandshakeLockCount > 0)
						mLock.wait();

					mUpdateLockCount++;
				}
			}
		}
		
		/**
		 * Releases the lock.
		 * @param handshake Whether or not it was a handshake lock.
		 */
		public void unlock(boolean handshake) {
			synchronized (mLock) {
				if (handshake && mHandshakeLockCount > 0)
					mHandshakeLockCount--;
				else if (!handshake && mUpdateLockCount > 0)
					mUpdateLockCount--;
				
				if (mHandshakeLockCount == 0 && mUpdateLockCount == 0)
					mLock.notifyAll();
			}
		}
	}
}
