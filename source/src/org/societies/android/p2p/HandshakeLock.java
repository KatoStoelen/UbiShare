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

/**
 * Lock ensuring that the server does not send updates while a handshake is
 * in progress. Multiple handshakes can be handled simultaneously, the same
 * goes for updates. The only restriction is that updates and handshakes does
 * not happen at the same time.
 * 
 * @author Kato
 */
public class HandshakeLock {
	
	/**
	 * Enum used for separating what type of lock is acquired.
	 */
	public enum LockType {
		/** Indicates a lock for a handshake operation. */
		HANDSHAKE,
		
		/** Indicates a lock for an update operation. */
		UPDATE
	}
	
	private final Object mLock = new Object();
	private int mUpdateLockCount = 0;
	private int mHandshakeLockCount = 0;
	
	/**
	 * Acquires the lock.
	 * @param lockType The type of lock to acquire.
	 * @throws InterruptedException If the thread is interrupted while waiting
	 * for lock.
	 */
	public void lock(LockType lockType) throws InterruptedException {
		synchronized (mLock) {
			if (lockType == LockType.HANDSHAKE) {
				while (mUpdateLockCount > 0)
					mLock.wait();

				mHandshakeLockCount++;
			} else if (lockType == LockType.UPDATE) {
				while (mHandshakeLockCount > 0)
					mLock.wait();

				mUpdateLockCount++;
			}
		}
	}
	
	/**
	 * Releases the lock.
	 * @param lockType The type of lock that is to be released.
	 */
	public void unlock(LockType lockType) {
		synchronized (mLock) {
			if (lockType == LockType.HANDSHAKE && mHandshakeLockCount > 0)
				mHandshakeLockCount--;
			else if (lockType == LockType.UPDATE && mUpdateLockCount > 0)
				mUpdateLockCount--;
			
			if (mHandshakeLockCount == 0 && mUpdateLockCount == 0)
				mLock.notifyAll();
		}
	}
}
