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

/**
 * Interface defining required functionality of connection listeners.
 * 
 * @author Kato
 */
interface IConnectionListener {
	
	/**
	 * The number of milliseconds before a call to acceptConnection times
	 * out and a <code>InterruptedIOException</code> is thrown.
	 * @see InterruptedIOException
	 */
	public static final int ACCEPT_TIMEOUT = 200;

	/**
	 * Closes the connection listener.
	 * @throws IOException If an error occurs while closing the listener.
	 */
	public void close() throws IOException;
	
	/**
	 * Initializes the connection listener. Required to accept connections
	 * from other devices.
	 * @throws IOException If an error occurs while initializing.
	 */
	public void initialize() throws IOException;
	
	/**
	 * Accepts an incoming connection. Blocks until a connection is found
	 * or the timeout value is exceeded.
	 * @return The established peer-to-peer connection.
	 * @see IP2PConnection#ACCEPT_TIMEOUT
	 * @throws IOException If an error occurs while accepting connection.
	 * @throws InterruptedIOException If the timeout value is exceeded.
	 */
	public IP2PConnection acceptConnection() throws IOException, InterruptedIOException;
}
