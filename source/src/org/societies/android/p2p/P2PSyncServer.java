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

import org.societies.android.p2p.entity.Request;
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
	
	/** The port number of the sync server. */
	public static final int PORT = 8888;
	
	private boolean mStopping;
	private Context mContext;
	private IConnectionListener mListener;
	
	/**
	 * Initializes a new sync server.
	 * @param context The context to use, cannot be <code>null</code>.
	 * @param listener The connection listener, cannot be <code>null</code>.
	 */
	public P2PSyncServer(Context context, IConnectionListener listener) {
		if (context == null)
			throw new IllegalArgumentException("Context cannot be null");
		if (listener == null)
			throw new IllegalArgumentException("Connection listener cannot be null");
		
		mContext = context;
		mListener = listener;
		mStopping = false;
	}

	@Override
	public void run() {
		try {
			mListener.initialize();
			
			while (!mStopping) {
				try {
					IP2PConnection connection = mListener.acceptConnection();
					
					if (connection != null)
						new ClientHandler(connection, mContext).start();
				} catch(InterruptedIOException e) { /* Ignore */ }
			}
		} catch (IOException e) {
			Log.e(TAG, e.getMessage(), e);
		} finally {
			try {
				mListener.close();
			} catch (IOException e) { /* Ignore */ }
		}
	}
	
	/**
	 * Stops the sync server.
	 */
	public void stopServer() {
		mStopping = true;
	}
	
	/**
	 * A worker thread that handles requests from clients.
	 * 
	 * @author Kato
	 */
	private class ClientHandler extends Thread {
		
		public static final String TAG = P2PSyncServer.TAG + ":ClientHandler";
		
		private IP2PConnection mConnection;
		private Context mContext;
		
		/**
		 * Initializes a new client handler.
		 * @param connection The peer-to-peer connection. Cannot
		 * be <code>null</code>.
		 * @param context The context to use.
		 */
		public ClientHandler(IP2PConnection connection, Context context) {
			if (connection == null)
				throw new IllegalArgumentException();
			
			mConnection = connection;
			mContext = context;
		}
		
		@Override
		public void run() {
			try {
				handleRequest(mConnection.readRequest());
			} catch (IOException e) {
				Log.e(TAG, e.getMessage(), e);
			} finally {
				try {
					mConnection.close();
				} catch (IOException e) { /* Ignore */ }
			}
		}
		
		/**
		 * Handles the received request.
		 * @param serializedData The serialized data received.
		 */
		private void handleRequest(Request request) {
			if (request == null) {
				Log.e(TAG, "Received request: null");
				return;
			}
			
			Log.i(TAG, "Request Type: " + request.getType());
			Log.i(TAG, "Last Request Time: " + request.getLastRequestTime());
			Log.i(TAG, "# Entities: " + request.getUpdatedEntities().size());
			
			for (Entity entity : request.getUpdatedEntities()) {
				entity.fetchLocalId(mContext.getContentResolver());
				
				/*
				if (entity.getId() == Entity.ENTITY_DEFAULT_ID)
					entity.insert(mContext.getContentResolver());
				else
					entity.update(mContext.getContentResolver());
				*/
				
				Log.i(TAG, entity.serialize());
			}
		}
	}
}
