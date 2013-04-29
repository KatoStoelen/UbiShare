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

import java.util.Locale;

import org.societies.android.p2p.P2PConnection.ConnectionType;

/**
 * A base class of P2P devices. This class is used to provide a generalization
 * of both Bluetooth and WiFi Direct devices.
 * 
 * @author Kato
 */
public abstract class P2PDevice {

	private final ConnectionType mConnectionType;
	
	/**
	 * Initiates a new P2P device.
	 * @param connectionType The connection type of the device.
	 */
	public P2PDevice(ConnectionType connectionType) {
		mConnectionType = connectionType;
	}
	
	/**
	 * Gets the connection type of the device.
	 * @return The connection type of the device.
	 */
	public ConnectionType getConnectionType() {
		return mConnectionType;
	}
	
	/**
	 * Gets the name of the device.
	 * @return The name of the device.
	 */
	public abstract String getName();
	
	/**
	 * Gets the address of the device.
	 * @return The address of the device.
	 */
	public abstract String getAddress();
	
	/**
	 * Gets the connection specific status of the device.
	 * @return The connection specific status of the device.
	 */
	public abstract String getConnectionStatus();
	
	/**
	 * Gets the type of the device.
	 * @return The type of the device.
	 */
	public abstract String getType();
	
	@Override
	public String toString() {
		return String.format(Locale.getDefault(), "%s (%s)", getName(), getAddress());
	}
}
