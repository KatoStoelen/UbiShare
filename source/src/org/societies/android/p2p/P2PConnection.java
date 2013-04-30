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
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InterruptedIOException;

import org.societies.android.p2p.entity.Request;
import org.societies.android.p2p.entity.Response;

import android.os.Parcelable;
import android.util.Log;

/**
 * Base class of all peer-to-peer connections.
 * 
 * @author Kato
 */
public abstract class P2PConnection implements Parcelable {
	
	public static final String TAG = "P2PConnection";
	
	/**
	 * An enum of supported connection types.
	 */
	public enum ConnectionType {
		/** Specifies Bluetooth as communication channel. */
		BLUETOOTH,
		
		/** Specifies WiFi Direct as communication channel. */
		WIFI_DIRECT
	}
	
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
	
	/** The new-line character of the system. */
	private static final String NEW_LINE = System.getProperty("line.separator");
	
	private final ConnectionType mConnectionType;
	
	protected BufferedWriter mWriter;
	protected BufferedReader mReader;
	protected boolean mInitialized = false;
	
	/**
	 * Initializes a new P2P connection.
	 * @param connectionType The type of the connection.
	 */
	protected P2PConnection(ConnectionType connectionType) {
		mConnectionType = connectionType;
	}
	
	/**
	 * Reads to the end of the stream.
	 * @return The read data as a string.
	 * @throws IOException If an error occurs while reading.
	 * @throws InterruptedIOException If the read times out.
	 */
	private String readToEnd() throws IOException, InterruptedIOException {
		if (!mInitialized)
			throw new IllegalStateException("Not initialized");
		
		StringBuilder builder = new StringBuilder();
		
		String line;
		while ((line = mReader.readLine()) != null)
			builder.append(line + NEW_LINE);
		
		return builder.toString();
	}
	
	/**
	 * Writes the specified string to the output stream of the connection.
	 * @param serialized The serialized request or response.
	 * @throws IOException IF an error occurs while writing.
	 */
	private void write(String serialized) throws IOException {
		if (!mInitialized)
			throw new IllegalStateException("Not initialized");
		
		mWriter.write(serialized);
		mWriter.newLine();
		mWriter.flush();
	}
	
	/**
	 * Reads a request from the input stream of the connection.
	 * @return The read request, or <code>null</code> if nothing was read.
	 * @throws IOException If an error occurs while reading.
	 * @throws InterruptedIOException If the read times out.
	 * @see P2PConnection#READ_TIMEOUT
	 */
	public Request readRequest() throws IOException, InterruptedIOException {
		String serialized = readToEnd();
		
		Log.i(TAG, "Received request: " + serialized);
		
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
		
		Log.i(TAG, "Received response: " + serialized);
		
		return Response.deserialize(serialized);
	}
	
	/**
	 * Writes a request to the output stream of the connection.
	 * @param request The request to write.
	 * @throws IOException If an error occurs while writing.
	 */
	public void write(Request request) throws IOException {
		write(request.serialize());
	}
	
	/**
	 * Writes a response to the output stream of the connection.
	 * @param response The response to write.
	 * @throws IOException If an error occurs while writing.
	 */
	public void write(Response response) throws IOException {
		write(response.serialize());
	}
	
	/**
	 * Closes the connection.
	 * @throws IOException If an error occurs while closing the connection.
	 */
	public void close() throws IOException {
		IOException lastException = null;
		
		try {
			if (mWriter != null)
				mWriter.close();
		} catch (IOException e) { lastException = e; }
		
		try {
			if (mReader != null)
				mReader.close();
		} catch (IOException e) { lastException = e; }
		
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
	
	/**
	 * Checks whether or not the connection is established.
	 * @return <code>true</code> if the connection is established, otherwise
	 * <code>false</code>.
	 */
	public abstract boolean isConnected();
}
