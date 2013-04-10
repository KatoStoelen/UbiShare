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

import org.societies.android.p2p.P2PConnection.ConnectionType;

import android.net.wifi.p2p.WifiP2pDevice;

/**
 * Class representing a WiFi Direct device.
 * 
 * @author Kato
 */
public class WiFiDirectP2PDevice extends P2PDevice {

	private final WifiP2pDevice mDevice;
	
	/**
	 * Initializes a new WiFi Direct device.
	 * @param device The WifiP2pDevice instance.
	 */
	public WiFiDirectP2PDevice(WifiP2pDevice device) {
		super(ConnectionType.WIFI_DIRECT);
		
		mDevice = device;
	}

	@Override
	public String getName() {
		return mDevice.deviceName;
	}

	@Override
	public String getAddress() {
		return mDevice.deviceAddress;
	}

	/**
	 * Gets the connection status of the device.
	 * @return The connection status of the device.
	 */
	@Override
	public int getConnectionStatus() {
		return mDevice.status;
	}

	@Override
	public String getType() {
		return mDevice.primaryDeviceType;
	}
	
	/**
	 * Whether or not the device is group owner.
	 * @return <code>true</code> if the device is group owner, otherwise
	 * <code>false</code>.
	 */
	public boolean isGroupOwner() {
		return mDevice.isGroupOwner();
	}
}
