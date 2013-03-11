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

import org.societies.android.p2p.P2PSyncManager.ConnectionStatus;
import org.societies.android.p2p.P2PSyncManager.ConnectionType;

/**
 * An interface defining required methods of P2P listeners.
 * 
 * @author Kato
 */
public interface IP2PListener {

	/**
	 * Called when new peers are available.
	 * @param peers The list of peers.
	 * @param completeList Whether or not the peers are a complete listing of
	 * available peers. If <code>false</code>, the given peers come in addition
	 * to any previously found peers.
	 */
	public void onPeersAvailable(List<P2PDevice> peers, boolean completeList);
	
	/**
	 * Called when the status of the P2P connection changes.
	 * @param status The status of the P2P connection.
	 * @param type The type of the P2P connection.
	 */
	public void onP2pConnectionStatusChange(ConnectionStatus status, ConnectionType type);
}
