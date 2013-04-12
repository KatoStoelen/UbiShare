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
import java.util.List;
import java.util.Queue;

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
		
		mPeers = new PeerList();
		mStopping = false;
	}

	@Override
	public void run() {
		// TODO: Handle HANDSHAKEs
		// TODO: Create broadcaster thread
	}
	
	/**
	 * Stops the sync server.
	 * @param awaitTermination Whether or not to block until the server
	 * has terminated.
	 * @throws InterruptedException If awaiting termination and thread is
	 * interrupted.
	 */
	public void stopServer(boolean awaitTermination) throws InterruptedException {
		mStopping = true;
		
		if (awaitTermination && isAlive())
			join();
	}
	
	/**
	 * Thread responsible for sending updates to the clients.
	 */
	private class ClientHandler extends Thread {
		// TODO: create Peer entity. UUID, IP, PORT, LAST_RECEIVED_UPDATE_TIME ++
	}
}
