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
import java.io.Serializable;

import org.societies.android.p2p.entity.Request;
import org.societies.android.p2p.entity.Response;

/**
 * Base class of all peer-to-peer connections.
 * 
 * @author Kato
 */
abstract class P2PConnection implements Serializable {
	
	/** Unique ID. */
	private static final long serialVersionUID = -7661483437019363736L;

	/**
	 * The number of milliseconds before a read call times out and a
	 * <code>InterruptedIOException</code> is thrown.
	 * @see InterruptedIOException
	 */
	public static final int READ_TIMEOUT = 3000;
	
	/**
	 * The number of milliseconds before a connection call times out and
	 * a <code>InterruptedIOException</code> is thrown.
	 * @see InterruptedIOException
	 */
	public static final int CONNECTION_TIMEOUT = 3000;
	
	/**
	 * Reads to the end of the stream.
	 * @return The read data as a string.
	 * @throws IOException If an error occurs while reading.
	 * @throws InterruptedIOException If the read times out.
	 */
	protected abstract String readToEnd() throws IOException, InterruptedIOException;
	
	/**
	 * Checks whether or not the connection is established.
	 * @return <code>true</code> if the connection is established, otherwise
	 * <code>false</code>.
	 */
	public abstract boolean isConnected();
	
	/**
	 * Tries to establish a connection with the remote host. If the connection
	 * is already established, the connection is closed and re-opened.
	 * @return Whether or not the connection was successfully made.
	 * @throws IOException If an error occurs while connecting.
	 * @throws InterruptedIOException If the connection attempt times out.
	 */
	public abstract boolean connect() throws IOException, InterruptedIOException;
	
	/**
	 * Reads a request from the input stream of the connection.
	 * @return The read request, or <code>null</code> if nothing was read.
	 * @throws IOException If an error occurs while reading.
	 * @throws InterruptedIOException If the read times out.
	 * @see P2PConnection#READ_TIMEOUT
	 */
	public Request readRequest() throws IOException, InterruptedIOException {
		String serialized = readToEnd();
		
		return Request.deserialize(serialized);
	}
	
	/**
	 * Reads a response from the input stream of the connection.
	 * @return The read response, or <code>null</code> if nothing was read.
	 * @throws IOException If an error occurs while reading.
	 * @throws InterruptedIOException If the read times out.
	 * @see P2PConnection#READ_TIMEOUT
	 */
	public Response readResponse() throws IOException, InterruptedIOException {
		String serialized = readToEnd();
		
		return Response.deserialize(serialized);
	}
	
	/**
	 * Writes a request to the output stream of the connection.
	 * @param request The request to write.
	 * @throws IOException If an error occurs while writing.
	 */
	public abstract void write(Request request) throws IOException;
	
	/**
	 * Writes a response to the output stream of the connection.
	 * @param response The response to write.
	 * @throws IOException If an error occurs while writing.
	 */
	public abstract void write(Response response) throws IOException;
	
	/**
	 * Closes the connection.
	 * @throws IOException If an error occurs while closing the connection.
	 */
	public abstract void close() throws IOException;
}
