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

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

/**
 * Broadcast receiver used to notify sync services of connection loss.
 * This way sync services can gracefully shut down when the connection
 * is broken.
 * <br><br>
 * Use
 * <code>ServiceBroadcastReceiver.getBroadcastReceiver(ConnectionType, ISyncService)</code>
 * to get the service broadcast receiver of the connection type in use. 
 * 
 * @author Kato
 * @see ServiceBroadcastReceiver#getBroadcastReceiver(ConnectionType, ISyncService)
 */
abstract class ServiceBroadcastReceiver extends BroadcastReceiver {
	
	private final ConnectionType mConnectionType;
	private final ISyncService mSyncService;
	
	/**
	 * Initializes a new service broadcast receiver.
	 * @param connectionType The connection type in use.
	 * @param syncService The sync service.
	 * @see ServiceBroadcastReceiver#getBroadcastReceiver(ConnectionType, ISyncService)
	 */
	protected ServiceBroadcastReceiver(
			ConnectionType connectionType, ISyncService syncService) {
		mConnectionType = connectionType;
		mSyncService = syncService;
	}
	
	/**
	 * Stops the synchronization service.
	 */
	protected void stopSyncService() {
		mSyncService.stopSync(mConnectionType, false);
	}

	@Override
	public abstract void onReceive(Context context, Intent intent);

	/**
	 * Gets the intent filter to be used when registering the broadcast receiver.
	 * @return An <code>IntentFilter</code>.
	 */
	public abstract IntentFilter getIntentFilter();
	
	/**
	 * Gets the service broadcast receiver of the specified connection type.
	 * @param connectionType The connection type in use.
	 * @param syncService The sync service.
	 * @return A <code>ServiceBroadcastReceiver</code>.
	 */
	public static ServiceBroadcastReceiver getBroadcastReceiver(
			ConnectionType connectionType, ISyncService syncService) {
		if (connectionType == ConnectionType.BLUETOOTH)
			return new BluetoothServiceBroadcastReceiver(syncService);
		else if (connectionType == ConnectionType.WIFI_DIRECT)
			return new WiFiDirectServiceBroadcastReceiver(syncService);
		else
			throw new IllegalArgumentException("Unknown connection type: "
					+ connectionType);
	}
}
