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

import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.URI;
import java.net.URISyntaxException;

import org.societies.android.p2p.ConnectionType;


import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

/**
 * Provides a Wi-Fi Direct communication channel.
 * 
 * @author Kato
 */
public class WiFiDirectConnection extends P2PConnection {
	
	public static final String TAG = P2PConnection.TAG + ":WiFiDirect";

	private Socket mSocket;
	private InetSocketAddress mRemoteAddress;
	
	private boolean mConnectRequired = true;
	
	/**
	 * Initializes a new WiFi Direct connection with an <b>established</b>
	 * connection to a remote host.
	 * @param socket The connected TCP socket.
	 * @throws IOException If an error occurs while initializing.
	 */
	public WiFiDirectConnection(Socket socket) throws IOException {
		super(ConnectionType.WIFI_DIRECT);
		
		initialize(socket);
		
		mConnectRequired = false;
	}
	
	/**
	 * Initializes an <b>unestablished</b> WiFi Direct connection. A call
	 * to <code>connect()</code> is required before using this
	 * connection.
	 * @param remoteAddress The address of the remote host.
	 * @see P2PConnection#connect()
	 */
	public WiFiDirectConnection(InetSocketAddress remoteAddress) {
		super(ConnectionType.WIFI_DIRECT);
		
		mRemoteAddress = remoteAddress;
	}
	
	/**
	 * Initializes an <b>unestablished</b> WiFi Direct connection. A call
	 * to <code>connect()</code> is required before using this
	 * connection.
	 * @param in The serialized object.
	 * @see P2PConnection#connect()
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
		
		setInputStream(mSocket.getInputStream());
		setOutputStream(mSocket.getOutputStream());
	}
	
	/**
	 * Checks whether or not a connection has been successfully made.
	 * @return <code>true</code> if the a connection has been made,
	 * otherwise <code>false</code>.
	 */
	private boolean isConnected() {
		return (mSocket != null && mSocket.isConnected());
	}

	@Override
	public boolean connect() throws IOException, InterruptedIOException {
		if (mConnectRequired && !isConnected()) {
			Log.i(TAG, "Connecting to: " + mRemoteAddress);
			
			Socket socket = new Socket();
			socket.connect(mRemoteAddress, CONNECTION_TIMEOUT);
			initialize(socket);
		}
		
		return isConnected();
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
		mSocket = null;
		
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
						mSocket.getRemoteSocketAddress()
						.toString().replace("/", new String());
				
				URI address = new URI("url://" + socketAddress);
				
				return address.getHost();
			} catch (URISyntaxException e) {
				Log.e(TAG, e.getMessage(), e);
				return null;
			}
		} else {
			return null;
		}
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
	 * Required by Parcelable.
	 * @see Parcelable
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
