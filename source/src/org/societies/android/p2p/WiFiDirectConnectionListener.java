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
import java.net.ServerSocket;
import java.net.Socket;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Connection listener for WiFi Direct.
 * 
 * @author Kato
 */
class WiFiDirectConnectionListener extends P2PConnectionListener {
	
	private int mPort;
	private ServerSocket mListener;
	private boolean mIsInitialized = false;
	
	/**
	 * Initializes a new connection listener on the specified port.
	 * @param port The port number to listen on.
	 */
	public WiFiDirectConnectionListener(int port) {
		super(ConnectionType.WIFI_DIRECT);
		
		mPort = port;
	}
	
	/**
	 * Initializes a new connection listener from the specified
	 * serialized object.
	 * @param in The serialized connection listener.
	 */
	private WiFiDirectConnectionListener(Parcel in) {
		super(ConnectionType.WIFI_DIRECT);
		
		mPort = in.readInt();
	}

	@Override
	public void close() throws IOException {
		if (mListener != null)
			mListener.close();
	}

	@Override
	public void initialize() throws IOException {
		mListener = new ServerSocket(mPort);
		mListener.setSoTimeout(ACCEPT_TIMEOUT);
		
		mIsInitialized = true;
	}

	@Override
	public P2PConnection acceptConnection() throws IOException, InterruptedIOException {
		if (!mIsInitialized)
			throw new IllegalStateException("Listener is not initialized");
		
		Socket clientSocket = mListener.accept();
		
		return new WiFiDirectConnection(clientSocket);
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
		dest.writeInt(mPort);
	}

	/**
	 * Required by Parcelable.
	 */
	public static final Parcelable.Creator<WiFiDirectConnectionListener> CREATOR =
			new Parcelable.Creator<WiFiDirectConnectionListener>() {
		/*
		 * (non-Javadoc)
		 * @see android.os.Parcelable.Creator#createFromParcel(android.os.Parcel)
		 */
		public WiFiDirectConnectionListener createFromParcel(Parcel source) {
			return new WiFiDirectConnectionListener(source);
		}

		/*
		 * (non-Javadoc)
		 * @see android.os.Parcelable.Creator#newArray(int)
		 */
		public WiFiDirectConnectionListener[] newArray(int size) {
			return new WiFiDirectConnectionListener[size];
		}
	};
}
