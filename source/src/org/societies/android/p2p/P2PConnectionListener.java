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

import android.os.Parcelable;

/**
 * Base class of connection listeners.
 * 
 * @author Kato
 */
abstract class P2PConnectionListener implements Parcelable {
	
	/**
	 * The number of milliseconds before a call to acceptConnection times
	 * out and a <code>InterruptedIOException</code> is thrown.
	 * @see InterruptedIOException
	 */
	public static final int ACCEPT_TIMEOUT = 300;
	
	private final ConnectionType mConnectionType;
	
	/**
	 * Initializes a new connection listener.
	 * @param connectionType The type of the connection.
	 */
	protected P2PConnectionListener(ConnectionType connectionType) {
		mConnectionType = connectionType;
	}

	/**
	 * Closes the connection listener.
	 * @throws IOException If an error occurs while closing the listener.
	 */
	public abstract void close() throws IOException;
	
	/**
	 * Initializes the connection listener. Required to accept connections
	 * from other devices.
	 * @throws IOException If an error occurs while initializing.
	 */
	public abstract void initialize() throws IOException;
	
	/**
	 * Accepts an incoming connection. Blocks until a connection is found
	 * or the timeout value is exceeded. A call to <code>initialize()</code> is
	 * required before using this method.
	 * @return The established peer-to-peer connection.
	 * @see P2PConnectionListener#initialize()
	 * @see P2PConnectionListener#ACCEPT_TIMEOUT
	 * @throws IOException If an error occurs while accepting connection.
	 * @throws InterruptedIOException If a timeout occurs.
	 */
	public abstract P2PConnection acceptConnection() throws IOException, InterruptedIOException;

	/**
	 * Gets the connection type.
	 * @return The connection type.
	 */
	public ConnectionType getConnectionType() {
		return mConnectionType;
	}
}
