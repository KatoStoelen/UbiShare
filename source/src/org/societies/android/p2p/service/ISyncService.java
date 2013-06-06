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

/**
 * Interface defining required methods of sync services.
 * 
 * @author Kato
 */
public interface ISyncService {

	/**
	 * Stops the sync service of the specified connection type.
	 * @param connectionType The connection type of the service to stop.
	 * @param awaitTermination Whether or not to block until the service
	 * has terminated.
	 */
	void stopSync(ConnectionType connectionType, boolean awaitTermination);
	
	/**
	 * Stops all the running synchronization services.
	 * @param awaitTermination Whether or not to block until all the services
	 * have terminated.
	 */
	void stopAllSync(boolean awaitTermination);
}