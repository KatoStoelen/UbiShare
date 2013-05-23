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

import java.util.List;

import org.societies.android.p2p.P2PSyncManager.P2PInterfaceStatus;
import org.societies.android.p2p.P2PSyncManager.SyncRole;
import org.societies.android.p2p.entity.P2PDevice;

/**
 * An interface defining required methods of P2P listeners.
 * 
 * @author Kato
 */
public interface IP2PChangeListener {
	
	/**
	 * Called when new peers are available.
	 * @param peers The list of peers.
	 * @param completeList Whether or not the peers are a complete listing of
	 * available peers. If <code>false</code>, the given peers come in addition
	 * to any previously found peers.
	 * @param sender The sender of the notification.
	 */
	public void onPeersAvailable(
			List<P2PDevice> peers, boolean completeList, Object sender);
	
	/**
	 * Called when the status of the P2P interface changes.
	 * @param status The status of the P2P interface.
	 * @param sender The sender of the notification.
	 */
	public void onP2pInterfaceStatusChange(P2PInterfaceStatus status, Object sender);
	
	/**
	 * Called when the current device info changes.
	 * @param device The new device info.
	 * @param sender The sender of the notification.
	 */
	public void onThisDeviceChange(P2PDevice device, Object sender);
	
	/**
	 * Called if the discovery of peers fails.
	 * @param reason The reason of failure.
	 * @param sender The sender of the notification.
	 */
	public void onDiscoverPeersFailure(String reason, Object sender);
	
	/**
	 * Called if the connection process fails.
	 * @param reason The reason of failure.
	 * @param sender The sender of the notification.
	 */
	public void onConnectionFailure(String reason, Object sender);
	
	/**
	 * Called when a connection to another device is successfully made.
	 * @param role The synchronization role of the current device.
	 * @param sender The sender of the notification.
	 */
	public void onConnectionSuccess(SyncRole role, Object sender);
	
	/**
	 * Called when a disconnect attempt fails.
	 * @param reason The reason of failure.
	 * @param sender The sender of the notification.
	 */
	public void onDisconnectFailure(String reason, Object sender);
	
	/**
	 * Called when a disconnect attempt has succeeded.
	 * @param sender The sender of the notification.
	 */
	public void onDisconnectSuccess(Object sender);
	
	/**
	 * Called when the synchronization has stopped after a call to
	 * <code>stopSync()</code>.
	 * @param sender The sender of the notification.
	 */
	public void onSyncStopped(Object sender);
}
