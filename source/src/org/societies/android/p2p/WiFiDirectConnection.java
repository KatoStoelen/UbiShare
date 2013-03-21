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
import java.io.OutputStreamWriter;
import java.net.Socket;

import org.societies.android.p2p.entity.Request;
import org.societies.android.p2p.entity.Response;

import android.util.Log;

/**
 * Provides a Wi-Fi Direct communication channel.
 * 
 * @author Kato
 */
class WiFiDirectConnection implements IP2PConnection {
	
	public static final String TAG = "WiFiDirectConnection";

	private final Socket mSocket;
	private final BufferedReader mReader;
	private final BufferedWriter mWriter;
	
	/**
	 * Initializes a new WiFi Direct connection with the specified
	 * socket.
	 * @param socket The TCP socket.
	 */
	public WiFiDirectConnection(Socket socket) {
		mSocket = socket;
		
		BufferedReader tempReader = null;
		BufferedWriter tempWriter = null;
		
		try {
			tempReader = new BufferedReader(
					new InputStreamReader(mSocket.getInputStream()));
			tempWriter = new BufferedWriter(
					new OutputStreamWriter(mSocket.getOutputStream()));
		} catch (IOException e) {
			Log.e(TAG, e.getMessage(), e);
		}
		
		mReader = tempReader;
		mWriter = tempWriter;
	}
	
	/**
	 * Reads to the end of the stream.
	 * @return The read data as a string.
	 * @throws IOException If an error occurs while reading.
	 */
	private String readToEnd() throws IOException {
		StringBuilder builder = new StringBuilder();
		
		String line;
		while ((line = mReader.readLine()) != null)
			builder.append(line);
		
		return builder.toString();
	}
	
	/* (non-Javadoc)
	 * @see org.societies.android.p2p.IP2PConnection#readRequest()
	 */
	public Request readRequest() throws IOException {
		String serialized = readToEnd();
		
		return Request.deserialize(serialized);
	}

	/* (non-Javadoc)
	 * @see org.societies.android.p2p.IP2PConnection#readResponse()
	 */
	public Response readResponse() throws IOException {
		String serialized = readToEnd();
		
		return Response.deserialize(serialized);
	}

	/* (non-Javadoc)
	 * @see org.societies.android.p2p.IP2PConnection#write(org.societies.android.p2p.entity.Request)
	 */
	public void write(Request request) throws IOException {
		mWriter.write(request.serialize());
	}

	/* (non-Javadoc)
	 * @see org.societies.android.p2p.IP2PConnection#write(org.societies.android.p2p.entity.Response)
	 */
	public void write(Response response) throws IOException {
		mWriter.write(response.serialize());
	}

	/* (non-Javadoc)
	 * @see org.societies.android.p2p.IP2PConnection#close()
	 */
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
}
