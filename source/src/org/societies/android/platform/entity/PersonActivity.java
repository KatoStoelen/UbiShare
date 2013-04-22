/**
 * Copyright 2012 UbiCollab.org
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
package org.societies.android.platform.entity;

import static org.societies.android.api.cis.SocialContract.PeopleActivity.*;

import java.util.List;

import org.societies.android.api.cis.SocialContract.People;

import com.google.renamedgson.annotations.Expose;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;

/**
 * A person activity entity.
 * 
 * @author Kato
 */
public class PersonActivity extends Entity {

	private long id = ENTITY_DEFAULT_ID;
	
	@Expose private String globalId;
	private long feedOwnerId;
	@Expose private String actor;
	@Expose private String object;
	@Expose private String verb;
	@Expose private String target;
	@Expose private long creationDate = System.currentTimeMillis() / 1000;
	@Expose private long lastModifiedDate = System.currentTimeMillis() / 1000;
	
	@Expose private String globalIdFeedOwner;
	
	/**
	 * Gets a list of all the "dirty" person activities.
	 * @param resolver The content resolver.
	 * @return A list of updated person activities.
	 * @throws Exception If an error occurs while fetching.
	 */
	public static List<PersonActivity> getUpdatedPersonActivities(
			ContentResolver resolver) throws Exception {
		List<PersonActivity> updatedActivities = Entity.getEntities(
				PersonActivity.class,
				resolver,
				CONTENT_URI,
				null,
				DIRTY + " = 1",
				null,
				null);
		
		for (PersonActivity activity : updatedActivities)
			activity.fetchGlobalIds(resolver);
		
		return updatedActivities;
	}
	
	/**
	 * Gets a list of all the person activities.
	 * @param resolver The content resolver.
	 * @return A list of person activities.
	 * @throws Exception If an error occurs while fetching.
	 */
	public static List<PersonActivity> getAllPersonActivities(
			ContentResolver resolver) throws Exception {
		List<PersonActivity> activities = Entity.getEntities(
				PersonActivity.class,
				resolver,
				CONTENT_URI,
				null,
				null,
				null,
				null);
		
		for (PersonActivity activity : activities)
			activity.fetchGlobalIds(resolver);
		
		return activities;
	}

	@Override
	protected void populate(Cursor cursor) {
		super.populate(cursor);
		
		setId(				Entity.getLong(cursor, _ID));
		setGlobalId(		Entity.getString(cursor, GLOBAL_ID));
		setFeedOwnerId(		Entity.getLong(cursor, _ID_FEED_OWNER));
		setActor(			Entity.getString(cursor, ACTOR));
		setObject(			Entity.getString(cursor, OBJECT));
		setVerb(			Entity.getString(cursor, VERB));
		setTarget(			Entity.getString(cursor, TARGET));
		setCreationDate(	Entity.getLong(cursor, CREATION_DATE));
		setLastModifiedDate(Entity.getLong(cursor, LAST_MODIFIED_DATE));
	}
	
	@Override
	protected ContentValues getEntityValues() {
		ContentValues values = super.getEntityValues();
		
		values.put(GLOBAL_ID, globalId);
		values.put(_ID_FEED_OWNER, feedOwnerId);
		values.put(ACTOR, actor);
		values.put(OBJECT, object);
		values.put(VERB, verb);
		values.put(TARGET, target);
		values.put(CREATION_DATE, creationDate);
		values.put(LAST_MODIFIED_DATE, lastModifiedDate);
		
		return values;
	}
	
	@Override
	protected Uri getContentUri() {
		return CONTENT_URI;
	}
	
	@Override
	public void fetchGlobalIds(ContentResolver resolver) {
		setGlobalIdFeedOwner(
				Entity.getGlobalId(
						People.CONTENT_URI,
						feedOwnerId,
						People.GLOBAL_ID,
						resolver));
	}
	
	@Override
	public void fetchLocalIds(ContentResolver resolver) {
		setId(Entity.getLocalId(CONTENT_URI, _ID, GLOBAL_ID, globalId, resolver));
		setFeedOwnerId(
				Entity.getLocalId(
						People.CONTENT_URI,
						People._ID,
						People.GLOBAL_ID,
						globalIdFeedOwner,
						resolver));
	}

	@Override
	public long getId() {
		return id;
	}
	
	@Override
	protected void setId(long id) {
		this.id = id;
	}
	
	@Override
	public String getGlobalId() {
		return globalId;
	}
	
	@Override
	public void setGlobalId(String globalId) {
		this.globalId = globalId;
	}
	
	public long getFeedOwnerId() {
		return feedOwnerId;
	}
	
	public void setFeedOwnerId(long feedOwnerId) {
		this.feedOwnerId = feedOwnerId;
	}
	
	public String getActor() {
		return actor;
	}
	
	public void setActor(String actor) {
		this.actor = actor;
	}
	
	public String getObject() {
		return object;
	}
	
	public void setObject(String object) {
		this.object = object;
	}
	
	public String getVerb() {
		return verb;
	}
	
	public void setVerb(String verb) {
		this.verb = verb;
	}
	
	public String getTarget() {
		return target;
	}
	
	public void setTarget(String target) {
		this.target = target;
	}
	
	public long getCreationDate() {
		return creationDate;
	}
	
	public void setCreationDate(long creationDate) {
		this.creationDate = creationDate;
	}

	public long getLastModifiedDate() {
		return lastModifiedDate;
	}

	public void setLastModifiedDate(long lastModifiedDate) {
		this.lastModifiedDate = lastModifiedDate;
	}

	public String getGlobalIdFeedOwner() {
		return globalIdFeedOwner;
	}

	public void setGlobalIdFeedOwner(String globalIdFeedOwner) {
		this.globalIdFeedOwner = globalIdFeedOwner;
	}

	@Override
	public boolean isAllGlobalIdsSet() {
		return isGlobalIdValid(globalId) &&
				isGlobalIdValid(globalIdFeedOwner);
	}
}
