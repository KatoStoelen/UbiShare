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
package org.societies.android.p2p.entity;

import org.societies.android.p2p.ConnectionType;

import android.bluetooth.BluetoothDevice;
import android.util.SparseArray;

/**
 * A class representing a Bluetooth device.
 * 
 * @author Kato
 */
public class BluetoothP2PDevice extends P2PDevice {
	
	/**
	 * A map containing connection status messages.
	 */
	private static final SparseArray<String> CONNECTION_STATUS;
	
	static {
		CONNECTION_STATUS = new SparseArray<String>();
		CONNECTION_STATUS.append(BluetoothDevice.BOND_NONE, "NOT BONDED");
		CONNECTION_STATUS.append(BluetoothDevice.BOND_BONDING, "BONDING");
		CONNECTION_STATUS.append(BluetoothDevice.BOND_BONDED, "BONDED");
	}
	
	private BluetoothDevice mDevice;
	
	/**
	 * Initializes a new Bluetooth device.
	 * @param device The BluetoothDevice instance.
	 */
	public BluetoothP2PDevice(BluetoothDevice device) {
		super(ConnectionType.BLUETOOTH);
		
		mDevice = device;
	}

	@Override
	public String getName() {
		return mDevice.getName();
	}

	@Override
	public String getAddress() {
		return mDevice.getAddress();
	}

	/**
	 * Gets the bond state of the device.
	 * @return The bond state of the device, or <code>null</code> if
	 * the bond state is unknown.
	 */
	@Override
	public String getConnectionStatus() {
		return CONNECTION_STATUS.get(mDevice.getBondState());
	}

	/**
	 * This property is unsupported for Bluetooth devices.
	 * @return An empty string.
	 */
	@Override
	public String getType() {
		return new String();
	}

}