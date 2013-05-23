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
package org.societies.android.p2p.net;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;
import java.io.OutputStream;

import org.societies.android.p2p.ConnectionType;
import org.societies.android.p2p.entity.Request;
import org.societies.android.p2p.entity.Response;

import android.os.Parcelable;

/**
 * Base class of all peer-to-peer connections.
 * 
 * @author Kato
 */
public abstract class P2PConnection implements Parcelable {
	
	public static final String TAG = "P2PConnection";
	
	/**
	 * The number of milliseconds before a read call times out and a
	 * <code>InterruptedIOException</code> is thrown.
	 * @see InterruptedIOException
	 */
	public static final int READ_TIMEOUT = 5000;
	
	/**
	 * The number of milliseconds before a connection call times out and
	 * a <code>InterruptedIOException</code> is thrown.
	 * @see InterruptedIOException
	 */
	public static final int CONNECTION_TIMEOUT = 5000;
	
	private final ConnectionType mConnectionType;
	
	private DataOutputStream mWriter;
	private DataInputStream mReader;
	private boolean mInitialized = false;
	
	/**
	 * Initializes a new P2P connection.
	 * @param connectionType The type of the connection.
	 */
	protected P2PConnection(ConnectionType connectionType) {
		mConnectionType = connectionType;
	}
	
	/**
	 * Sets the input stream of the connection. Subclasses needs to
	 * set both I/O stream before the connection is usable.
	 * @param in The input stream.
	 * @see P2PConnection#setOutputStream(OutputStream)
	 */
	protected void setInputStream(InputStream in) {
		mReader = new DataInputStream(in);
		
		setInitialized();
	}
	
	/**
	 * Sets the output stream of the connection. Subclasses needs to
	 * set both I/O stream before the connection is usable.
	 * @param out The output stream.
	 * @see P2PConnection#setInputStream(InputStream)
	 */
	protected void setOutputStream(OutputStream out) {
		mWriter = new DataOutputStream(out);
		
		setInitialized();
	}
	
	/**
	 * Marks the connection as initialized if both I/O streams are
	 * set.
	 */
	private void setInitialized() {
		mInitialized = (mReader != null && mWriter != null);
	}
	
	/**
	 * Throws an <code>IllegalStateException</code> if the connection is
	 * not initialized.
	 * @throws IllegalStateException If the connection is not initialized.
	 */
	private void throwIfNotInitialized() throws IllegalStateException {
		if (!mInitialized)
			throw new IllegalStateException("Connection is either closed or not initialized");
	}
	
	/**
	 * Reads a string from the input stream.
	 * @return The read string.
	 * @throws IOException If an error occurs while reading.
	 * @throws InterruptedIOException If a timeout occurs while reading.
	 */
	private String readString() throws IOException, InterruptedIOException {
		throwIfNotInitialized();
		
		int bufferLength = mReader.readInt();
		byte[] buffer = new byte[bufferLength];
		
		mReader.readFully(buffer);
		
		return new String(buffer);
	}

	/**
	 * Writes the specified string to the output stream of the connection.
	 * @param serialized The serialized request or response.
	 * @throws IOException If an error occurs while writing.
	 */
	private void write(String serialized) throws IOException {
		throwIfNotInitialized();
		
		byte[] buffer = serialized.getBytes();
		
		mWriter.writeInt(buffer.length);
		mWriter.write(buffer);
		mWriter.flush();
	}
	
	/**
	 * Receives a request from the remote host.
	 * @return The received request, or <code>null</code> if nothing was received.
	 * @throws IOException If an error occurs while receiving.
	 * @throws InterruptedIOException If the receive times out.
	 * @see P2PConnection#READ_TIMEOUT
	 */
	public Request receiveRequest() throws IOException, InterruptedIOException {
		String serialized = readString();
		
		return Request.deserialize(serialized);
	}
	
	/**
	 * Receives a response from the remote host.
	 * @return The received response, or <code>null</code> if nothing was received.
	 * @throws IOException If an error occurs while receiving.
	 * @throws InterruptedIOException If the receive times out.
	 * @see P2PConnection#READ_TIMEOUT
	 */
	public Response receiveResponse() throws IOException, InterruptedIOException {
		String serialized = readString();
		
		return Response.deserialize(serialized);
	}
	
	/**
	 * Sends a request to the remote host.
	 * @param request The request to send.
	 * @throws IOException If an error occurs while sending.
	 */
	public void send(Request request) throws IOException {
		write(request.serialize());
	}
	
	/**
	 * Sends a response to the remote host.
	 * @param response The response to send.
	 * @throws IOException If an error occurs while sending.
	 */
	public void send(Response response) throws IOException {
		write(response.serialize());
	}
	
	/**
	 * Closes the connection.
	 * @throws IOException If an error occurs while closing the connection.
	 */
	public void close() throws IOException {
		mInitialized = false;
		
		IOException lastException = null;
		
		try {
			if (mWriter != null)
				mWriter.close();
		} catch (IOException e) { lastException = e; }
		mWriter = null;
		
		try {
			if (mReader != null)
				mReader.close();
		} catch (IOException e) { lastException = e; }
		mReader = null;
		
		if (lastException != null)
			throw lastException;
	}

	/**
	 * Gets the type of the P2P connection.
	 * @return The type of the connection.
	 */
	public ConnectionType getConnectionType() {
		return mConnectionType;
	}
	
	/**
	 * Tries to establish a connection with the remote host. If the connection
	 * is already established, the connection is closed and re-opened.
	 * @return Whether or not the connection was successfully made.
	 * @throws IOException If an error occurs while connecting.
	 * @throws InterruptedIOException If the connection attempt times out.
	 * @see P2PConnection#CONNECTION_TIMEOUT
	 */
	public abstract boolean connect() throws IOException, InterruptedIOException;
}
