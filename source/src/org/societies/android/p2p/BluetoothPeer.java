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
import java.util.UUID;

import org.societies.android.p2p.net.BluetoothConnection;
import org.societies.android.p2p.net.P2PConnection;

import android.bluetooth.BluetoothDevice;

/**
 * Class representing a Bluetooth peer.
 * 
 * @author Kato
 */
class BluetoothPeer extends Peer {

	private final BluetoothDevice mDevice;
	
	/**
	 * Initializes a new Bluetooth peer.
	 * @param uniqueId The unique ID of the peer.
	 * @param device The Bluetooth device related to this peer.
	 */
	public BluetoothPeer(String uniqueId, BluetoothDevice device) {
		super(uniqueId, ConnectionType.BLUETOOTH);
		
		mDevice = device;
	}

	@Override
	public P2PConnection connect() throws IOException, InterruptedIOException {
		P2PConnection connection = new BluetoothConnection(
				mDevice, UUID.fromString(getUniqueId()));
		connection.connect();
		
		return connection;
	}
}
