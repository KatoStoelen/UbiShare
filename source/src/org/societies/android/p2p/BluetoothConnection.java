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
import java.util.UUID;

import org.societies.android.p2p.entity.Request;
import org.societies.android.p2p.entity.Response;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.Parcel;
import android.os.Parcelable;

/**
 * Provides a Bluetooth communication channel.
 * 
 * @author Kato
 */
class BluetoothConnection extends P2PConnection {

	private BluetoothDevice mDevice;
	private UUID mServiceId;
	private BluetoothSocket mSocket;
	
	private BufferedReader mReader;
	private BufferedWriter mWriter;
	
	private boolean mInitialized = false;

	/**
	 * Initializes a new Bluetooth connection.
	 * @param socket The established connection.
	 * @throws IOException If an error occurs while initializing streams.
	 */
	public BluetoothConnection(BluetoothSocket socket) throws IOException {
		super(ConnectionType.BLUETOOTH);
		
		initialize(socket);
	}
	
	/**
	 * Initializes an unestablished Bluetooth connection. A call to
	 * <code>connect()</code> is required before using this connection.
	 * @param device The Bluetooth device of the remote host.
	 * @param serviceId The UUID of the remote service.
	 * @see BluetoothConnection#connect()
	 */
	public BluetoothConnection(BluetoothDevice device, UUID serviceId) {
		super(ConnectionType.BLUETOOTH);
		
		mDevice = device;
		mServiceId = serviceId;
	}
	
	/**
	 * Initializes an unestablished Bluetooth connection. A call to
	 * <code>connect()</code> is required before using this connection.
	 * @param in The serialized object.
	 * @see BluetoothConnection#connect()
	 */
	private BluetoothConnection(Parcel in) {
		super(ConnectionType.BLUETOOTH);
		
		mDevice = in.readParcelable(BluetoothDevice.class.getClassLoader());
		mServiceId = UUID.fromString(in.readString());
	}
	
	/**
	 * Initializes the Bluetooth connection.
	 * @param socket The established connection;
	 * @throws IOException If an error occurs while initializing streams.
	 */
	private void initialize(BluetoothSocket socket) throws IOException {
		mSocket = socket;
		mDevice = mSocket.getRemoteDevice();
		
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
		
		BluetoothSocket socket =
				mDevice.createRfcommSocketToServiceRecord(mServiceId);
		socket.connect();
		
		initialize(socket);
		
		return isConnected();
	}
	
	/**
	 * Gets the remote device of this connection.
	 * @return The remote device or this connection, or <code>null</code>
	 * if not connected.
	 */
	public BluetoothDevice getRemoteDevice() {
		return mDevice;
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
		dest.writeParcelable(mDevice, flags);
		dest.writeString(mServiceId.toString());
	}
	
	/**
	 * This field is required by Parcelable.
	 */
	public static final Parcelable.Creator<BluetoothConnection> CREATOR =
			new Creator<BluetoothConnection>() {
		/*
		 * (non-Javadoc)
		 * @see android.os.Parcelable.Creator#newArray(int)
		 */
		public BluetoothConnection[] newArray(int size) {
			return new BluetoothConnection[size];
		}
		
		/*
		 * (non-Javadoc)
		 * @see android.os.Parcelable.Creator#createFromParcel(android.os.Parcel)
		 */
		public BluetoothConnection createFromParcel(Parcel source) {
			return new BluetoothConnection(source);
		}
	};
}
