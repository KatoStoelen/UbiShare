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

import org.societies.android.p2p.net.BluetoothConnection;
import org.societies.android.p2p.net.P2PConnection;
import org.societies.android.p2p.net.WiFiDirectConnection;

/**
 * Base class of a peer in a peer-to-peer network.
 * 
 * @author Kato
 */
abstract class Peer {
	
	private final String mUniqueId;
	private final ConnectionType mConnectionType;
	private int mLastUpdateTime;
	private boolean mActive;
	
	/**
	 * Initiates a new peer.
	 * @param uniqueId The unique ID of the peer.
	 * @param connectionType The connection type of the peer.
	 */
	protected Peer(String uniqueId, ConnectionType connectionType) {
		mUniqueId = uniqueId;
		mConnectionType = connectionType;
		mLastUpdateTime = 0;
		mActive = true;
	}
	
	/**
	 * Establishes a connection to the peer.
	 * @return The established connection.
	 * @throws IOException If an error occurs while connecting.
	 * @throws InterruptedIOException If the connection attempt times out.
	 */
	public abstract P2PConnection connect()
			throws IOException, InterruptedIOException;

	/**
	 * Gets the unique ID of the peer.
	 * @return A string representing the unique ID of the peer.
	 */
	public String getUniqueId() {
		return mUniqueId;
	}

	/**
	 * Gets the connection type of the peer.
	 * @return The <code>ConnectionType</code> of the peer.
	 */
	public ConnectionType getConnectionType() {
		return mConnectionType;
	}

	/**
	 * Gets the last update time of the peer.
	 * @return An integer representing the last update time (UNIX time in
	 * seconds) of the peer.
	 */
	public int getLastUpdateTime() {
		return mLastUpdateTime;
	}

	/**
	 * Sets the last update time of the peer.
	 * @param lastUpdateTime The UNIX time, in seconds, of the last update
	 * time.
	 */
	public void setLastUpdateTime(int lastUpdateTime) {
		mLastUpdateTime = lastUpdateTime;
	}
	
	/**
	 * Sets the last update time to the current time.
	 */
	public void setLastUpdateTimeNow() {
		setLastUpdateTime((int)(System.currentTimeMillis() / 1000));
	}

	/**
	 * Whether or not the client is active.
	 * @return <code>true</code> if the client is active, otherwise
	 * <code>false</code>.
	 */
	public boolean isActive() {
		return mActive;
	}

	/**
	 * Sets whether or not the client is active.
	 * @param active Whether or not the client is active.
	 */
	public void setActive(boolean active) {
		mActive = active;
	}
	
	/**
	 * Gets a new peer with the specified unique ID.
	 * @param uniqueId The unique ID of the peer.
	 * @param connection The connection to the peer.
	 * @return A new <code>Peer</code>.
	 */
	public static Peer getPeer(String uniqueId, P2PConnection connection) {
		if (connection.getConnectionType() == ConnectionType.BLUETOOTH) {
			BluetoothConnection bluetoothConnection =
					(BluetoothConnection) connection;
			
			return new BluetoothPeer(
					uniqueId, bluetoothConnection.getRemoteDevice());
		} else if (connection.getConnectionType() == ConnectionType.WIFI_DIRECT) {
			WiFiDirectConnection wifiDirectConnection =
					(WiFiDirectConnection) connection;
			
			return new WiFiDirectPeer(
					uniqueId,
					wifiDirectConnection.getRemoteIp(),
					P2PConstants.WIFI_DIRECT_CLIENT_PORT);
		} else {
			throw new IllegalArgumentException("Unknown connection type: " +
					connection.getConnectionType());
		}
	}
}
