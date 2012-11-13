/**
 * Copyright 2012 UbiCollab
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
package org.societies.android.sync.box;

import java.io.IOException;
import java.util.Date;
import java.util.List;

import org.societies.android.box.BoxConstants;
import org.societies.android.platform.entity.Community;
import org.societies.android.platform.entity.CommunityActivity;
import org.societies.android.platform.entity.Membership;
import org.societies.android.platform.entity.Person;
import org.societies.android.platform.entity.PersonActivity;
import org.societies.android.platform.entity.Relationship;

import com.box.androidlib.DAO.Update;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SyncResult;
import android.os.Bundle;
import android.util.Log;

/**
 * Adapter for synchronizing SocialProvider data with Box.
 * 
 * @author Kato
 */
public class BoxSyncAdapter extends AbstractThreadedSyncAdapter {
	
	private static final String TAG = "BoxSyncAdapter";
	
	private ContentResolver mResolver;
	private AccountManager mAccountManager;
	private BoxHandler mBoxHandler;
	private SharedPreferences mPreferences;

	/**
	 * Initiates a new BoxSyncAdapter.
	 * @param context The context to operate in.
	 * @param autoInitialize Whether or not to auto initialize the adapter.
	 */
	public BoxSyncAdapter(Context context, boolean autoInitialize) {
		super(context, autoInitialize);
		
		mPreferences = context.getSharedPreferences(
				BoxConstants.PREFERENCE_FILE, Context.MODE_PRIVATE);
		mResolver = context.getContentResolver();
		mAccountManager = AccountManager.get(context);
		mBoxHandler = new BoxHandler(mPreferences, mResolver);
	}
	
	@Override
	public void onSyncCanceled() {
		mBoxHandler.cancelRunningOperations();
		
		super.onSyncCanceled();
	}
	
	@Override
	public void onPerformSync(
			Account account,
			Bundle extras,
			String authority,
			ContentProviderClient provider,
			SyncResult syncResult) {
		try {
			String authToken = mAccountManager.blockingGetAuthToken(
				account, BoxConstants.AUTH_TOKEN_FLAG, true);
			
			mBoxHandler.initialize(authToken);
			
			// TODO: Figure out the deleted files part
			long lastSync = mPreferences.getLong(BoxConstants.PREFERENCE_LAST_SYNC, 0);
			Log.i(TAG, "Last sync: " + new Date(lastSync * 1000));
			processBoxUpdates(lastSync);
			
			Log.i(TAG, "Waiting for running operations to complete.");
			mBoxHandler.waitForRunningOperationsToComplete();
			Log.i(TAG, "Running operations complete.");
			
			// TODO: Sync only updated entities
			syncPeople();
			syncPeopleActivities();
			
			syncCommunities();
			syncCommunityActivities();
			
			syncMemberships();
			syncRelationships();
			
			Log.i(TAG, "Waiting for running operations to complete.");
			mBoxHandler.waitForRunningOperationsToComplete();
			Log.i(TAG, "Running operations complete.");
			
			mPreferences.edit().putLong(
					BoxConstants.PREFERENCE_LAST_SYNC,
					new Date().getTime() / 1000
			).commit();
		} catch (Exception e) {
			Log.e(TAG, e.getMessage(), e);
		}
	}

	/**
	 * Processes the updates from Box.
	 * @param lastSync The timestamp of the last synchronization.
	 * @throws IOException If an error occurs while fetching updates.
	 */
	private void processBoxUpdates(long lastSync)
			throws IOException {
		List<Update> updates = null;
		if (lastSync > 0)
			updates = mBoxHandler.getUpdatesSince(lastSync);
		
		if (updates == null)
			Log.i(TAG, "Fetching all entities from box.");
		else
			Log.i(TAG, "Fetching " + updates.size() + " updates from box.");
		
		mBoxHandler.processUpdates(updates);
	}
	
	/**
	 * Synchronizes the people.
	 */
	private void syncPeople() {
		Log.i(TAG, "Started People Sync");
		
		List<Person> people = Person.getUpdatedPeople(mResolver);
		
		for (Person person : people) {
			mBoxHandler.uploadEntity(person);
		}
	}
	
	/**
	 * Synchronizes the people activities.
	 */
	private void syncPeopleActivities() {
		Log.i(TAG, "Started Person Activities Sync");
		
		List<PersonActivity> activities =
				PersonActivity.getUpdatedPersonActivities(mResolver);
		
		for (PersonActivity activity : activities)
			mBoxHandler.uploadEntity(activity);
	}
	
	/**
	 * Synchronizes the communities.
	 */
	private void syncCommunities() {
		Log.i(TAG, "Started Communities Sync");
		
		List<Community> communities =
				Community.getUpdatedCommunities(mResolver);
		
		for (Community community : communities)
			mBoxHandler.uploadEntity(community);
	}
	
	/**
	 * Synchronizes the community activities.
	 */
	private void syncCommunityActivities() {
		Log.i(TAG, "Started Community Activities Sync");
		
		List<CommunityActivity> activities =
				CommunityActivity.getUpdatedCommunityActivities(mResolver);
		
		for (CommunityActivity activity : activities)
			mBoxHandler.uploadEntity(activity);
	}
	
	/**
	 * Synchronizes the memberships.
	 */
	private void syncMemberships() {
		Log.i(TAG, "Started Memberships Sync");
		
		List<Membership> memberships =
				Membership.getUpdatedMemberships(mResolver);
		
		for (Membership membership : memberships)
			mBoxHandler.uploadEntity(membership);
	}
	
	/**
	 * Synchronizes the relationships.
	 */
	private void syncRelationships() {
		Log.i(TAG, "Started Relationships Sync");
		
		List<Relationship> relationships =
				Relationship.getUpdatedRelationships(mResolver);
		
		for (Relationship relationship : relationships)
			mBoxHandler.uploadEntity(relationship);
	}
}
