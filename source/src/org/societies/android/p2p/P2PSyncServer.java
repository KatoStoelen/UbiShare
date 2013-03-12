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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.InterruptedIOException;
import java.lang.reflect.Type;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Collection;

import org.societies.android.platform.entity.Entity;
import org.societies.android.platform.entity.EntityTypeAdapter;

import com.google.renamedgson.Gson;
import com.google.renamedgson.GsonBuilder;
import com.google.renamedgson.reflect.TypeToken;

import android.content.ContentResolver;
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
	/** The number of milliseconds */
	private static final int ACCEPT_TIMEOUT = 200;
	
	private boolean mStopping;
	private Context mContext;
	
	/**
	 * Initializes a new sync server.
	 */
	public P2PSyncServer(Context context) {
		mContext = context;
		mStopping = false;
	}

	@Override
	public void run() {
		ServerSocket serverSocket = null;
		try {
			serverSocket = new ServerSocket(PORT);
			serverSocket.setSoTimeout(ACCEPT_TIMEOUT);
			
			while (!mStopping) {
				try {
					Socket clientSocket = serverSocket.accept();
					
					new ClientHandler(clientSocket, mContext).start();
				} catch(InterruptedIOException e) { /* Ignore */ }
			}
		} catch (IOException e) {
			Log.e(TAG, e.getMessage(), e);
		} finally {
			if (serverSocket != null) {
				try {
					serverSocket.close();
				} catch (IOException e) { /* Ignore */ }
			}
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
		
		private Socket mClientSocket;
		private Context mContext;
		
		/**
		 * Initializes a new client handler.
		 * @param clientSocket The client connection representing socket. Cannot
		 * be <code>null</code>.
		 * @param context The context to use.
		 */
		public ClientHandler(Socket clientSocket, Context context) {
			if (clientSocket == null)
				throw new IllegalArgumentException();
			
			mClientSocket = clientSocket;
			mContext = context;
		}
		
		@Override
		public void run() {
			BufferedReader reader = null;
			try {
				reader = new BufferedReader(
						new InputStreamReader(mClientSocket.getInputStream()));
				
				StringBuilder builder = new StringBuilder();
				
				String line;
				while ((line = reader.readLine()) != null)
					builder.append(line);
				
				handleData(builder.toString());
			} catch (IOException e) {
				Log.e(TAG, e.getMessage(), e);
			} finally {
				if (reader != null) {
					try {
						reader.close();
					} catch (IOException e) { /* Ignore */ }
				}
				
				closeConnection();
			}
		}
		
		/**
		 * Handles the data received from the client.
		 * @param serializedData The serialized data received.
		 */
		private void handleData(String serializedData) {
			Gson serializer = new GsonBuilder()
				.registerTypeAdapter(Entity.class, new EntityTypeAdapter())
				.create();
			
			Type collectionType = new TypeToken<Collection<Entity>>(){}.getType();
			
			Collection<Entity> entities = serializer.fromJson(
					serializedData, collectionType);
			
			for (Entity entity : entities) {
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
		
		/**
		 * Closes the connection to the client.
		 */
		private void closeConnection() {
			try {
				mClientSocket.close();
			} catch (IOException e) { /* Ignore */ }
		}
	}
}
