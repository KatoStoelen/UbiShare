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

import java.util.ArrayList;
import java.util.Iterator;

/**
 * A list of peers.
 * 
 * @author Kato
 */
class PeerList extends ArrayList<Peer> {

	/** Unique Id. */
	private static final long serialVersionUID = -2322771170730376896L;

	/**
	 * Gets the peer with the specified unique ID.
	 * @param uniqueId The unique ID of the peer.
	 * @return The peer with the specified unique ID, or <code>null</code>
	 * if it does not exist.
	 */
	public Peer getPeer(String uniqueId) {
		for (Iterator<Peer> it = listIterator(); it.hasNext();) {
			Peer peer = it.next();
			
			if (peer.getUniqueId().equals(uniqueId))
				return peer;
		}
		
		return null;
	}
}
