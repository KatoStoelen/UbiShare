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
package org.societies.android.p2p.service;

import org.societies.android.p2p.ConnectionType;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

/**
 * Broadcast receiver used to notify sync services of Bluetooth connection
 * loss. This way sync services can gracefully shut down when the Bluetooth
 * connection is broken.
 * 
 * @author Kato
 */
class BluetoothServiceBroadcastReceiver extends ServiceBroadcastReceiver {

	/**
	 * Initializes a new Bluetooth service broadcast receiver.
	 * @param syncService The sync service.
	 */
	protected BluetoothServiceBroadcastReceiver(ISyncService syncService) {
		super(ConnectionType.BLUETOOTH, syncService);
	}

	@Override
	public void onReceive(Context context, Intent intent) {
		// TODO IMPLEMENT
	}

	@Override
	public IntentFilter getIntentFilter() {
		// TODO IMPLEMENT
		return null;
	}
}
