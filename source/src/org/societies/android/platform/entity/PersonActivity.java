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
package org.societies.android.platform.entity;

import static org.societies.android.api.cis.SocialContract.PeopleActivity.*;

import java.util.List;

import android.content.ContentResolver;
import android.database.Cursor;

/**
 * A person activity entity.
 * 
 * @author Kato
 */
public class PersonActivity extends Entity {

	private String id;
	private String globalId;
	private String globalIdFeedOwner;
	private String globalIdActor;
	private String globalIdObject;
	private String globalIdVerb;
	private String globalIdTarget;
	private String creationDate;
	private String lastModifiedDate;
	
	/**
	 * Gets a list of all the person activities in the database.
	 * @param resolver The content resolver.
	 * @return A list of all the person activities in the database.
	 */
	public static List<PersonActivity> getPersonActivities(ContentResolver resolver) {
		return Entity.getEntities(
				PersonActivity.class, resolver, CONTENT_URI, null, null, null, null);
	}

	@Override
	protected void populate(Cursor cursor) {
		setId(Entity.getString(cursor, _ID));
		setGlobalId(Entity.getString(cursor, GLOBAL_ID));
		setGlobalIdFeedOwner(Entity.getString(cursor, GLOBAL_ID_FEED_OWNER));
		setGlobalIdActor(Entity.getString(cursor, GLOBAL_ID_ACTOR));
		setGlobalIdObject(Entity.getString(cursor, GLOBAL_ID_OBJECT));
		setGlobalIdVerb(Entity.getString(cursor, GLOBAL_ID_VERB));
		setGlobalIdTarget(Entity.getString(cursor, GLOBAL_ID_TARGET));
		setCreationDate(Entity.getString(cursor, CREATION_DATE));
		//setLastModifiedDate(Entity.getString(cursor, LAST_MODIFIED_DATE)); TODO: add last modified date?
	}
	

	@Override
	public void update(ContentResolver resolver) {
		// TODO Auto-generated method stub
	}

	@Override
	public void delete(ContentResolver resolver) {
		// TODO Auto-generated method stub
	}

	@Override
	public void insert(ContentResolver resolver) {
		// TODO Auto-generated method stub
	}

	@Override
	public String serialize() {
		StringBuilder builder = new StringBuilder();
		
		builder.append(String.format(SERIALIZE_FORMAT, GLOBAL_ID, globalId));
		builder.append(String.format(SERIALIZE_FORMAT, GLOBAL_ID_FEED_OWNER, globalIdFeedOwner));
		builder.append(String.format(SERIALIZE_FORMAT, GLOBAL_ID_ACTOR, globalIdActor));
		builder.append(String.format(SERIALIZE_FORMAT, GLOBAL_ID_OBJECT, globalIdObject));
		builder.append(String.format(SERIALIZE_FORMAT, GLOBAL_ID_VERB, globalIdVerb));
		builder.append(String.format(SERIALIZE_FORMAT, GLOBAL_ID_TARGET, globalIdTarget));
		builder.append(String.format(SERIALIZE_FORMAT, CREATION_DATE, creationDate));
		//builder.append(String.format(SERIALIZE_FORMAT, LAST_MODIFIED_DATE, lastModifiedDate)); TODO: add last modified date?
		
		return builder.toString();
	}
	
	public String getId() {
		return id;
	}
	
	public void setId(String id) {
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
	
	public String getGlobalIdFeedOwner() {
		return globalIdFeedOwner;
	}
	
	public void setGlobalIdFeedOwner(String globalIdFeedOwner) {
		this.globalIdFeedOwner = globalIdFeedOwner;
	}
	
	public String getGlobalIdActor() {
		return globalIdActor;
	}
	
	public void setGlobalIdActor(String globalIdActor) {
		this.globalIdActor = globalIdActor;
	}
	
	public String getGlobalIdObject() {
		return globalIdObject;
	}
	
	public void setGlobalIdObject(String globalIdObject) {
		this.globalIdObject = globalIdObject;
	}
	
	public String getGlobalIdVerb() {
		return globalIdVerb;
	}
	
	public void setGlobalIdVerb(String globalIdVerb) {
		this.globalIdVerb = globalIdVerb;
	}
	
	public String getGlobalIdTarget() {
		return globalIdTarget;
	}
	
	public void setGlobalIdTarget(String globalIdTarget) {
		this.globalIdTarget = globalIdTarget;
	}
	
	public String getCreationDate() {
		return creationDate;
	}
	
	public void setCreationDate(String creationDate) {
		this.creationDate = creationDate;
	}
	
	public String getLastModifiedDate() {
		return lastModifiedDate;
	}
	
	public void setLastModifiedDate(String lastModifiedDate) {
		this.lastModifiedDate = lastModifiedDate;
	}
}
