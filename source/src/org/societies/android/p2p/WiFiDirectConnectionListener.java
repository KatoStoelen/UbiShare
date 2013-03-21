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
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Connection listener for WiFi Direct.
 * 
 * @author Kato
 */
public class WiFiDirectConnectionListener implements IConnectionListener {
	
	private int mPort;
	private ServerSocket mListener;
	private boolean mIsInitialized;
	
	/**
	 * Initializes a new connection listener on the specified port.
	 * @param port The port number to listen on.
	 */
	public WiFiDirectConnectionListener(int port) {
		mPort = port;
		mIsInitialized = false;
	}

	/* (non-Javadoc)
	 * @see org.societies.android.p2p.IConnectionListener#close()
	 */
	public void close() throws IOException {
		if (mListener != null)
			mListener.close();
	}

	/* (non-Javadoc)
	 * @see org.societies.android.p2p.IConnectionListener#initialize()
	 */
	public void initialize() throws IOException {
		mListener = new ServerSocket(mPort);
		mListener.setSoTimeout(ACCEPT_TIMEOUT);
		
		mIsInitialized = true;
	}

	/* (non-Javadoc)
	 * @see org.societies.android.p2p.IConnectionListener#acceptConnection()
	 */
	public IP2PConnection acceptConnection() throws IOException, InterruptedIOException {
		if (!mIsInitialized)
			throw new IllegalStateException("Listener not initialized");
		
		Socket clientSocket = mListener.accept();
		
		return new WiFiDirectConnection(clientSocket);
	}

}
