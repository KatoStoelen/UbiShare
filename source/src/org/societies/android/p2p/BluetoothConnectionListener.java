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

import org.societies.android.p2p.P2PConnection.ConnectionType;

/**
 * Bluetooth connection listener.
 * 
 * @author Kato
 */
public class BluetoothConnectionListener extends P2PConnectionListener {

	/** Unique ID. */
	private static final long serialVersionUID = 7877091133368946345L;
	
	/**
	 * Initializes a new Bluetooth connection listener.
	 */
	protected BluetoothConnectionListener() {
		super(ConnectionType.BLUETOOTH);
	}

	@Override
	public void close() throws IOException {
		// TODO Auto-generated method stub

	}

	@Override
	public void initialize() throws IOException {
		// TODO Auto-generated method stub

	}

	@Override
	public P2PConnection acceptConnection() throws IOException,
			InterruptedIOException {
		return null;
	}

}
