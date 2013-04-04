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
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;

import org.societies.android.p2p.entity.Request;
import org.societies.android.p2p.entity.Response;

import android.util.Log;

/**
 * Provides a Wi-Fi Direct communication channel.
 * 
 * @author Kato
 */
class WiFiDirectConnection extends P2PConnection {
	
	public static final String TAG = "WiFiDirectConnection";

	private Socket mSocket;
	private InetAddress mRemoteAddress;
	
	private BufferedReader mReader;
	private BufferedWriter mWriter;
	
	private boolean mInitialized = false;
	
	/**
	 * Initializes a new WiFi Direct connection with an open connection
	 * to a remote host.
	 * @param socket The connected TCP socket.
	 * @throws IOException If an error occurs while initializing.
	 */
	public WiFiDirectConnection(Socket socket) throws IOException {
		initialize(socket);
	}
	
	/**
	 * Initializes an unestablished WiFi Direct connection. A call
	 * to <code>connect()</code> is required before using this
	 * connection.
	 * @param remoteAddress The address of the remote host.
	 */
	public WiFiDirectConnection(InetAddress remoteAddress) {
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
	public void connect() throws IOException, InterruptedIOException {
		if (isConnected())
			close();
		
		Socket socket = new Socket();
		socket.connect(
				new InetSocketAddress(mRemoteAddress, P2PSyncServer.PORT),
				CONNECTION_TIMEOUT);
		
		initialize(socket);
	}
}
