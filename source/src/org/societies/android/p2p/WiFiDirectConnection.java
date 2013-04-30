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


import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

/**
 * Provides a Wi-Fi Direct communication channel.
 * 
 * @author Kato
 */
class WiFiDirectConnection extends P2PConnection {
	
	public static final String TAG = P2PConnection.TAG + ":WiFiDirect";

	private Socket mSocket;
	private InetSocketAddress mRemoteAddress;
	
	private boolean mConnectRequired = true;
	
	/**
	 * Initializes a new WiFi Direct connection with an open connection
	 * to a remote host.
	 * @param socket The connected TCP socket.
	 * @throws IOException If an error occurs while initializing.
	 */
	public WiFiDirectConnection(Socket socket) throws IOException {
		super(ConnectionType.WIFI_DIRECT);
		
		initialize(socket);
		
		mConnectRequired = false;
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
	 * Initializes an unestablished WiFi Direct connection. A call
	 * to <code>connect()</code> is required before using this
	 * connection.
	 * @param in The serialized object.
	 */
	private WiFiDirectConnection(Parcel in) {
		super(ConnectionType.WIFI_DIRECT);
		
		mRemoteAddress = new InetSocketAddress(in.readString(), in.readInt());
	}
	
	/**
	 * Initializes the socket and IO streams.
	 * @param socket The connected TCP socket.
	 * @throws IOException If an error occurs while initializing.
	 */
	private void initialize(Socket socket) throws IOException {
		mSocket = socket;
		mSocket.setSoTimeout(READ_TIMEOUT);
		mSocket.setTcpNoDelay(true);
		
		mReader = new BufferedReader(
				new InputStreamReader(mSocket.getInputStream()));
		mWriter = new BufferedWriter(
				new OutputStreamWriter(mSocket.getOutputStream()));
		
		mInitialized = true;
	}
	
	@Override
	public boolean isConnected() {
		return (mSocket != null && mSocket.isConnected());
	}

	@Override
	public boolean connect() throws IOException, InterruptedIOException {
		if (mConnectRequired) {
			if (isConnected())
				close();
			
			Log.i(TAG, "Connecting to: " + mRemoteAddress);
			
			Socket socket = new Socket();
			socket.connect(mRemoteAddress, CONNECTION_TIMEOUT);
			initialize(socket);
			
			return isConnected();
		} else {
			return false;
		}
	}
	
	@Override
	public void close() throws IOException {
		IOException lastException = null;
		
		try {
			super.close();
		} catch (IOException e) { lastException = e; }
		
		try {
			if (mSocket != null)
				mSocket.close();
		} catch (IOException e) { lastException = e; }
		
		if (lastException != null)
			throw lastException;
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

	/* (non-Javadoc)
	 * @see android.os.Parcelable#describeContents()
	 */
	public int describeContents() {
		return 0;
	}

	/* (non-Javadoc)
	 * @see android.os.Parcelable#writeToParcel(android.os.Parcel, int)
	 */
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(mRemoteAddress.getHostName());
		dest.writeInt(mRemoteAddress.getPort());
	}
	
	/**
	 * This field is required by Parcelable.
	 */
	public static final Parcelable.Creator<WiFiDirectConnection> CREATOR =
			new Parcelable.Creator<WiFiDirectConnection>() {
		/*
		 * (non-Javadoc)
		 * @see android.os.Parcelable.Creator#createFromParcel(android.os.Parcel)
		 */
		public WiFiDirectConnection createFromParcel(Parcel source) {
			return new WiFiDirectConnection(source);
		}
		
		/*
		 * (non-Javadoc)
		 * @see android.os.Parcelable.Creator#newArray(int)
		 */
		public WiFiDirectConnection[] newArray(int size) {
			return new WiFiDirectConnection[size];
		}
	};
}
