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

import java.io.IOException;
import java.io.InterruptedIOException;

/**
 * Bluetooth connection listener.
 * 
 * @author Kato
 */
public class BluetoothConnectionListener implements IConnectionListener {

	/** Unique ID. */
	private static final long serialVersionUID = 7877091133368946345L;

	/* (non-Javadoc)
	 * @see org.societies.android.p2p.IConnectionListener#close()
	 */
	public void close() throws IOException {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see org.societies.android.p2p.IConnectionListener#initialize()
	 */
	public void initialize() throws IOException {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see org.societies.android.p2p.IConnectionListener#acceptConnection()
	 */
	public P2PConnection acceptConnection() throws IOException,
			InterruptedIOException {
		return null;
	}

}
