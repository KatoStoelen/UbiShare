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

import java.util.List;

import org.societies.android.api.cis.SocialContract.People;

import com.google.renamedgson.annotations.Expose;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;

import static org.societies.android.api.cis.SocialContract.Relationship.*;

/**
 * A relationship entity.
 * 
 * @author Kato
 */
public class Relationship extends Entity {

	private long id = ENTITY_DEFAULT_ID;
	
	@Expose private String globalId;
	private long p1Id;
	private long p2Id;
	@Expose private String type;
	@Expose private long creationDate = System.currentTimeMillis() / 1000;
	@Expose private long lastModifiedDate = System.currentTimeMillis() / 1000;
	
	@Expose private String globalIdP1;
	@Expose private String globalIdP2;
	
	/**
	 * Gets a list of all the "dirty" relationships.
	 * @param resolver The content resolver.
	 * @return A list of updated relationships.
	 * @throws Exception If an error occurs while fetching.
	 */
	public static List<Relationship> getUpdatedRelationships(
			ContentResolver resolver) throws Exception {
		List<Relationship> updatedRelationships = Entity.getEntities(
				Relationship.class,
				resolver,
				CONTENT_URI,
				null,
				DIRTY + " = 1",
				null,
				null);
		
		for (Relationship relationship : updatedRelationships)
			relationship.fetchGlobalIds(resolver);
		
		return updatedRelationships;
	}
	
	/**
	 * Gets a list of all the relationships.
	 * @param resolver The content resolver.
	 * @return A list of relationships.
	 * @throws Exception If an error occurs while fetching.
	 */
	public static List<Relationship> getAllRelationships(
			ContentResolver resolver) throws Exception {
		List<Relationship> relationships = Entity.getEntities(
				Relationship.class,
				resolver,
				CONTENT_URI,
				null,
				null,
				null,
				null);
		
		for (Relationship relationship : relationships)
			relationship.fetchGlobalIds(resolver);
		
		return relationships;
	}

	@Override
	protected void populate(Cursor cursor) {
		super.populate(cursor);
		
		setId(				Entity.getLong(cursor, _ID));
		setGlobalId(		Entity.getString(cursor, GLOBAL_ID));
		setP1Id(			Entity.getLong(cursor, _ID_P1));
		setP2Id(			Entity.getLong(cursor, _ID_P2));
		setType(			Entity.getString(cursor, TYPE));
		setCreationDate(	Entity.getLong(cursor, CREATION_DATE));
		setLastModifiedDate(Entity.getLong(cursor, LAST_MODIFIED_DATE));
	}

	@Override
	protected ContentValues getEntityValues() {
		ContentValues values = super.getEntityValues();
		
		values.put(GLOBAL_ID, globalId);
		values.put(_ID_P1, p1Id);
		values.put(_ID_P2, p2Id);
		values.put(TYPE, type);
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
		setGlobalIdP1(
				Entity.getGlobalId(
						People.CONTENT_URI,
						p1Id,
						People.GLOBAL_ID,
						resolver));
		setGlobalIdP2(
				Entity.getGlobalId(
						People.CONTENT_URI,
						p2Id,
						People.GLOBAL_ID,
						resolver));
	}
	
	@Override
	public void fetchLocalIds(ContentResolver resolver) {
		setId(Entity.getLocalId(CONTENT_URI, _ID, GLOBAL_ID, globalId, resolver));
		setP1Id(Entity.getLocalId(
				People.CONTENT_URI,
				People._ID,
				People.GLOBAL_ID,
				globalIdP1,
				resolver));
		setP2Id(Entity.getLocalId(
				People.CONTENT_URI,
				People._ID,
				People.GLOBAL_ID,
				globalIdP2,
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
	
	public long getP1Id() {
		return p1Id;
	}
	
	public void setP1Id(long p1Id) {
		this.p1Id = p1Id;
	}
	
	public long getP2Id() {
		return p2Id;
	}
	
	public void setP2Id(long p2Id) {
		this.p2Id = p2Id;
	}
	
	public String getType() {
		return type;
	}
	
	public void setType(String type) {
		this.type = type;
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

	public String getGlobalIdP1() {
		return globalIdP1;
	}

	public void setGlobalIdP1(String globalIdP1) {
		this.globalIdP1 = globalIdP1;
	}

	public String getGlobalIdP2() {
		return globalIdP2;
	}

	public void setGlobalIdP2(String globalIdP2) {
		this.globalIdP2 = globalIdP2;
	}

	@Override
	public boolean isAllGlobalIdsSet() {
		return isGlobalIdValid(globalId) &&
				isGlobalIdValid(globalIdP1) &&
				isGlobalIdValid(globalIdP2);
	}
}
