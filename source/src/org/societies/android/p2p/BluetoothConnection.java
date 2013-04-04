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

import org.societies.android.p2p.entity.Request;
import org.societies.android.p2p.entity.Response;

/**
 * Provides a Bluetooth communication channel.
 * 
 * @author Kato
 */
class BluetoothConnection extends P2PConnection {

	@Override
	protected String readToEnd() throws IOException, InterruptedIOException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void write(Request request) throws IOException {
		// TODO Auto-generated method stub
	}

	@Override
	public void write(Response response) throws IOException {
		// TODO Auto-generated method stub
	}

	@Override
	public void close() throws IOException {
		// TODO Auto-generated method stub
	}

	@Override
	public boolean isConnected() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void connect() throws IOException, InterruptedIOException {
		// TODO Auto-generated method stub
	}
}
