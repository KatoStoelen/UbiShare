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
import java.util.Collection;
import java.util.UUID;

import org.societies.android.platform.entity.Entity;

import android.content.Context;
import android.util.Log;

/**
 * A thread that polls the database checking for updated entities
 * and notifies listeners if any are found.
 * 
 * @author Kato
 */
class UpdatePoller extends Thread {
	
	/**
	 * Interface defining required methods of update listeners.
	 */
	public interface UpdateListener {
		/**
		 * Called when updated entities are available.
		 * @param entities The updated entities.
		 */
		void onEntitiesAvailable(Collection<Entity> entities);
	}
	
	public static final String TAG = "UpdatePoller";

	/** The interval (in milliseconds) between each poll for updated entities. */
	private static final int POLL_INTERVAL = 5000;
	
	private final Context mContext;
	private final UpdateListener mListener;
	private boolean mStopping;
	
	/**
	 * Initializes a new update poller.
	 * @param listener The listener to notify of updated entities.
	 */
	public UpdatePoller(Context context, UpdateListener listener) {
		mContext = context;
		mListener = listener;
		mStopping = false;
	}
	
	@Override
	public void run() {
		Log.i(TAG, "UpdatePoller started");
		
		try {
			while (!mStopping) {
				Collection<Entity> updatedEntities = processGlobalIds(
						Entity.getUpdatedEntities(mContext.getContentResolver()));
				
				if (updatedEntities.size() > 0)
					mListener.onEntitiesAvailable(updatedEntities);
				
				Thread.sleep(POLL_INTERVAL);
			}
		} catch (InterruptedException e) {
			if (!mStopping)
				Log.e(TAG, "Interrupted while sleeping");
		} catch (Exception e) {
			Log.e(TAG, e.getMessage(), e);
		}
		
		Log.i(TAG, "UpdatePoller terminated");
	}
	
	/**
	 * Resets the dirty flag of the specified entities.
	 * @param entities The entities to reset dirty flag of.
	 */
	public void resetEntityDirtyFlag(Collection<Entity> entities) {
		for (Entity entity : entities) {
			entity.setDirtyFlag(0);
			entity.insert(mContext.getContentResolver());
		}
	}

	/**
	 * Stops the update poller.
	 */
	public void stopPolling() {
		mStopping = true;
		
		if (getState() == State.TIMED_WAITING);
			interrupt();
	}
	
	/**
	 * Processes the global IDs of the entities, ensuring that all of the are set.
	 * @param entities The entities to process.
	 * @return The list of entities that have all global IDs set.
	 */
	private Collection<Entity> processGlobalIds(Collection<Entity> entities) {
		Collection<Entity> processedEntities = new ArrayList<Entity>();
		
		for (Entity entity : entities) {
			entity.fetchGlobalIds(mContext.getContentResolver());
			
			if (!Entity.isGlobalIdValid(entity.getGlobalId())) {
				entity.setGlobalId(UUID.randomUUID().toString());
				entity.update(mContext.getContentResolver());
			}
			
			if (entity.isAllGlobalIdsSet())
				processedEntities.add(entity);
			else
				Log.i(TAG, "Dropped entity due to lack of global IDs");
		}
		
		return processedEntities;
	}
}
