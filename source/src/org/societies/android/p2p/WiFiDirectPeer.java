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
import java.net.InetSocketAddress;

import org.societies.android.p2p.net.P2PConnection;
import org.societies.android.p2p.net.WiFiDirectConnection;

/**
 * Class representing a WiFi Direct peer.
 * 
 * @author Kato
 */
class WiFiDirectPeer extends Peer {
	
	private InetSocketAddress mAddress;

	/**
	 * Initializes a new WiFi Direct peer.
	 * @param uniqueId The unique ID of the peer.
	 * @param ip The IP address of the peer.
	 * @param port The port number of the peer.
	 */
	public WiFiDirectPeer(String uniqueId, String ip, int port) {
		super(uniqueId, ConnectionType.WIFI_DIRECT);
		
		mAddress = new InetSocketAddress(ip, port);
	}

	@Override
	public P2PConnection connect() throws IOException, InterruptedIOException {
		P2PConnection connection = new WiFiDirectConnection(getAddress());
		connection.connect();
		
		return connection;
	}

	/**
	 * Gets the address of the peer.
	 * @return The <code>InetSocketAddress</code> of the peer.
	 */
	public InetSocketAddress getAddress() {
		return mAddress;
	}

	/**
	 * Sets the address of the peer.
	 * @param address The address of the peer.
	 */
	public void setAddress(InetSocketAddress address) {
		mAddress = address;
	}
}
