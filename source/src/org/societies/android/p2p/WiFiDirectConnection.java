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
import java.io.InputStreamReader;
import java.io.InterruptedIOException;
import java.io.OutputStreamWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.URI;
import java.net.URISyntaxException;

import org.societies.android.p2p.entity.Request;
import org.societies.android.p2p.entity.Response;

/**
 * Provides a Wi-Fi Direct communication channel.
 * 
 * @author Kato
 */
class WiFiDirectConnection extends P2PConnection {
	
	/** Unique ID. */
	private static final long serialVersionUID = -2071972758995959880L;

	public static final String TAG = "WiFiDirectConnection";

	transient private Socket mSocket;
	private InetSocketAddress mRemoteAddress;
	
	transient private BufferedReader mReader;
	transient private BufferedWriter mWriter;
	
	private boolean mInitialized = false;
	
	/**
	 * Initializes a new WiFi Direct connection with an open connection
	 * to a remote host.
	 * @param socket The connected TCP socket.
	 * @throws IOException If an error occurs while initializing.
	 */
	public WiFiDirectConnection(Socket socket) throws IOException {
		super(ConnectionType.WIFI_DIRECT);
		
		initialize(socket);
	}
	
	/**
	 * Initializes an unestablished WiFi Direct connection. A call
	 * to <code>connect()</code> is required before using this
	 * connection.
	 * @param remoteAddress The address of the remote host.
	 */
	public WiFiDirectConnection(InetSocketAddress remoteAddress) {
		super(ConnectionType.WIFI_DIRECT);
		
		mRemoteAddress = remoteAddress;
	}
	
	/**
	 * Initializes the socket and IO streams.
	 * @param socket The connected TCP socket.
	 * @throws IOException If an error occurs while initializing.
	 */
	private void initialize(Socket socket) throws IOException {
		mSocket = socket;
		mSocket.setSoTimeout(READ_TIMEOUT);
		
		mReader = new BufferedReader(
				new InputStreamReader(mSocket.getInputStream()));
		mWriter = new BufferedWriter(
				new OutputStreamWriter(mSocket.getOutputStream()));
		
		mInitialized = true;
	}
	
	@Override
	protected String readToEnd() throws IOException, InterruptedIOException {
		if (!mInitialized)
			throw new IllegalStateException("Not initialized");
		
		StringBuilder builder = new StringBuilder();
		String newline = System.getProperty("line.separator");
		
		String line;
		while ((line = mReader.readLine()) != null)
			builder.append(line + newline);
		
		return builder.toString();
	}
	
	@Override
	public void write(Request request) throws IOException {
		if (!mInitialized)
			throw new IllegalStateException("Not initialized");
		
		mWriter.write(request.serialize());
		mWriter.flush();
	}

	@Override
	public void write(Response response) throws IOException {
		if (!mInitialized)
			throw new IllegalStateException("Not initialized");
		
		mWriter.write(response.serialize());
		mWriter.flush();
	}

	@Override
	public void close() throws IOException {
		IOException lastException = null;
		
		try {
			if (mReader != null) mReader.close();
		} catch (IOException e) { lastException = e; }
		
		try {
			if (mWriter != null) mWriter.close();
		} catch (IOException e) { lastException = e; }
		
		try {
			if (mSocket != null) mSocket.close();
		} catch (IOException e) { lastException = e; }
		
		if (lastException != null)
			throw lastException;
	}
	
	@Override
	public boolean isConnected() {
		return (mSocket != null && mSocket.isConnected());
	}

	@Override
	public boolean connect() throws IOException, InterruptedIOException {
		if (isConnected())
			close();
		
		Socket socket = new Socket();
		socket.connect(mRemoteAddress, CONNECTION_TIMEOUT);
		
		initialize(socket);
		
		return isConnected();
	}
	
	/**
	 * Gets the IP address of the remote host.
	 * @return The IP address or the remote host, or <code>null</code>
	 * if not connected.
	 */
	public String getRemoteIp() {
		if (isConnected()) {
			try {
				String socketAddress =
						mSocket.getRemoteSocketAddress().toString();
				URI address = new URI("my://" + socketAddress);
				
				return address.getHost();
			} catch (URISyntaxException e) {
				return null;
			}
		}
		else return null;
	}
}
