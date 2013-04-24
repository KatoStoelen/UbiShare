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

import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.IntentFilter;

/**
 * Bluetooth sync manager.
 * 
 * @author Kato
 */
class BluetoothSyncManager extends P2PSyncManager {

	/**
	 * Initializes a new Bluetooth sync manager.
	 * @param context The context to use.
	 * @param changeListener The listener to notify of P2P changes.
	 */
	public BluetoothSyncManager(Context context, IP2PChangeListener changeListener) {
		super(context, ConnectionType.BLUETOOTH, changeListener);
	}

	@Override
	protected BroadcastReceiver getBroadcastReceiver() {
		return new BluetoothBroadcastReceiver();
	}

	@Override
	protected IntentFilter getIntentFilter() {
		IntentFilter filter = new IntentFilter();
		
		filter.addAction(BluetoothDevice.ACTION_FOUND);
		
		return filter;
	}

	@Override
	public void discoverPeers() {
		// TODO: IMPLEMENT
	}

	@Override
	public void connectTo(P2PDevice device) {
		// TODO: IMPLEMENT
	}

	@Override
	protected P2PConnectionListener getServerConnectionListener() {
		return new BluetoothConnectionListener();
	}

	@Override
	public void disconnect() {
		// TODO: IMPLEMENT
	}
}
