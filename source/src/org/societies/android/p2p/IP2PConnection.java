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

import org.societies.android.p2p.entity.Request;
import org.societies.android.p2p.entity.Response;

/**
 * An interface defining required methods of peer-to-peer connections.
 * 
 * @author Kato
 */
interface IP2PConnection {
	
	/**
	 * Reads a request from the input stream of the connection.
	 * @return The read request, or <code>null</code> if nothing was read.
	 * @throws IOException If an error occurs while reading.
	 */
	public Request readRequest() throws IOException;
	
	/**
	 * Reads a response from the input stream of the connection.
	 * @return The read response, or <code>null</code> if nothing was read.
	 * @throws IOException If an error occurs while reading.
	 */
	public Response readResponse() throws IOException;
	
	/**
	 * Writes a request to the output stream of the connection.
	 * @param request The request to write.
	 * @throws IOException If an error occurs while writing.
	 */
	public void write(Request request) throws IOException;
	
	/**
	 * Writes a response to the output stream of the connection.
	 * @param response The response to write.
	 * @throws IOException If an error occurs while writing.
	 */
	public void write(Response response) throws IOException;
	
	/**
	 * Closes the connection.
	 * @throws IOException If an error occurs while closing the connection.
	 */
	public void close() throws IOException;
}
