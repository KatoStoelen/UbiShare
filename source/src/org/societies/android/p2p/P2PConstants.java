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
	
	/** The name of the Bluetooth service of the server. */
	public static final String BLUETOOTH_SERVICE_NAME_SERVER = "UbiShareP2P_Server";
	/** The name of the Bluetooth service of the client. */
	public static final String BLUETOOTH_SERVICE_NAME_CLIENT = "UbiShareP2P_Client";
	/** The UUID of the Bluetooth service of the server. */
	public static final UUID BLUETOOTH_SERVICE_UUID_SERVER = UUID.fromString("3f07178f-451e-4ef8-8b71-a78a5abe0e85");
	
	/** The name of the preference file. */
	public static final String PREFERENCE_FILE = "p2p.pref";
	/** The name of the unique ID preference. */
	public static final String PREFERENCE_UNIQUE_ID = "unique_id";
}
