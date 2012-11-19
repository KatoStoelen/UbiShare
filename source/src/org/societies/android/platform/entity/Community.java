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

import java.util.List;

import static org.societies.android.api.cis.SocialContract.Communities.*;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;

/**
 * A community entity.
 * 
 * @author Kato
 */
public class Community extends Entity {

	private int id = -1;
	private String globalId;
	private String name;
	private String ownerId;
	private String type;
	private String description;
	private String creationDate;
	private String lastModifiedDate;
	
	/**
	 * Gets a list of all the communities that have been updated since the last
	 * synchronization.
	 * @param lastSync The Unix time (in seconds) of the last synchronization.
	 * @param resolver The content resolver.
	 * @return A list of updated communities.
	 */
	public static List<Community> getUpdatedCommunities(
			long lastSync, ContentResolver resolver) {
		return Entity.getEntities(
				Community.class,
				resolver,
				CONTENT_URI,
				null,
				null, /*LAST_MODIFIED_DATE + " > ?",*/
				null, /*new String[] { String.valueOf(lastSync) },*/
				null);
	}
	
	@Override
	protected void populate(Cursor cursor) {
		setId(					Entity.getInt(cursor, _ID));
		setGlobalId(			Entity.getString(cursor, GLOBAL_ID));
		setName(				Entity.getString(cursor, NAME));
		setOwnerId(				Entity.getString(cursor, OWNER_ID));
		setType(				Entity.getString(cursor, TYPE));
		setDescription(			Entity.getString(cursor, DESCRIPTION));
		setCreationDate(		Entity.getString(cursor, CREATION_DATE));
		setLastModifiedDate(	Entity.getString(cursor, LAST_MODIFIED_DATE));
	}
	
	@Override
	protected ContentValues getEntityValues() {
		ContentValues values = new ContentValues();
		
		values.put(GLOBAL_ID, globalId);
		values.put(NAME, name);
		values.put(OWNER_ID, ownerId);
		values.put(TYPE, type);
		values.put(DESCRIPTION, description);
		values.put(CREATION_DATE, creationDate);
		values.put(LAST_MODIFIED_DATE, lastModifiedDate);
		
		return values;
	}
	
	@Override
	protected Uri getContentUri() {
		return CONTENT_URI;
	}
	
	@Override
	public void fetchLocalId(ContentResolver resolver) {
		setId(Entity.getLocalId(CONTENT_URI, _ID, GLOBAL_ID, globalId, resolver));
	}

	@Override
	public int getId() {
		return id;
	}
	
	@Override
	protected void setId(int id) {
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
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public String getOwnerId() {
		return ownerId;
	}
	
	public void setOwnerId(String ownerId) {
		this.ownerId = ownerId;
	}
	
	public String getType() {
		return type;
	}
	
	public void setType(String type) {
		this.type = type;
	}
	
	public String getDescription() {
		return description;
	}
	
	public void setDescription(String description) {
		this.description = description;
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