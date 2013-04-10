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

import java.util.UUID;

/**
 * Interface containing constants related to P2P synchronizing.
 * 
 * @author Kato
 */
public interface P2PConstants {
	/** The port number of the WiFi Direct sync server. */
	public static final int WIFI_DIRECT_SERVER_PORT = 8888;
	/** The port number of the WiFi Direct sync client. */
	public static final int WIFI_DIRECT_CLIENT_PORT = 8889;
	
	/** The name of the Bluetooth service. */
	public static final String BLUETOOTH_SERVICE_NAME = "UbiShareP2P";
	/** The UUID of the Bluetooth service. */
	public static final UUID BLUETOOTH_SERVICE_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
}
