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

import java.util.ArrayList;
import java.util.List;

import com.google.renamedgson.Gson;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

/**
 * Base class of entities.
 * 
 * @author Kato
 */
public abstract class Entity {
	
	private static final String TAG = "Entity";
	
	/** The format in which properties should be serialized. */
	protected static final String SERIALIZE_FORMAT = "%s=%s\n";
	/** The default local ID of an entity. */
	protected static final int ENTITY_DEFAULT_ID = -1;
	
	/**
	 * Removes the entity with the specified global ID from the database.
	 * @param entityClass The class of the entity.
	 * @param globalId The global ID of the entity.
	 * @param resolver The content resolver.
	 * @return The number of rows deleted in the database.
	 */
	public static <E extends Entity> int deleteEntity(
			Class<E> entityClass, String globalId, ContentResolver resolver) {
		int rowsDeleted = 0;
		
		try {
			E entity = entityClass.newInstance();
			entity.setGlobalId(globalId);
			entity.fetchLocalId(resolver);
			
			rowsDeleted = entity.delete(resolver);
		} catch (Exception e) {
			Log.e(TAG, e.getMessage(), e);
		}
		
		return rowsDeleted;
	}
	
	/**
	 * Gets a list of entities of the specified type.
	 * @param entityClass The class of the entity.
	 * @param resolver The content resolver.
	 * @param contentUri The URL to the content to retrieve.
	 * @param projection A list of which columns to return, or null for all columns.
	 * @param selection A filter declaring which rows to return, or null for all rows.
	 * @param selectionArgs The replacement values for any ?s in the selection filter.
	 * @param sortOrder The sort order, or null for default order.
	 * @return A list of entities of the specified type.
	 */
	protected static <E extends Entity> List<E> getEntities(
			Class<E> entityClass,
			ContentResolver resolver,
			Uri contentUri,
			String[] projection,
			String selection,
			String[] selectionArgs,
			String sortOrder) {
		List<E> entities = new ArrayList<E>();
		
		Cursor cursor = null;
		try {
			cursor = resolver.query(contentUri, projection, selection, selectionArgs, sortOrder);
			
			if (cursor.moveToFirst()) {
				for (boolean hasItem = true; hasItem; hasItem = cursor.moveToNext()) {
					E entity = entityClass.newInstance();
					entity.populate(cursor);
					
					entities.add(entity);
				}
			}
		} catch (Exception e) {
			Log.e(TAG, e.getMessage(), e);
		} finally {
			if (cursor != null)
				cursor.close();
		}
		
		return entities;
	}
	
	/**
	 * Gets the local ID of the entity with the specified global ID.
	 * @param contentUri The content URL.
	 * @param idColumnName The name of the ID column.
	 * @param globalIdColumnName The name of the global ID column.
	 * @param globalId The global ID of the entity.
	 * @param resolver The content resolver.
	 * @return The local ID of the entity with the specified global ID, or
	 * {@link Entity#ENTITY_DEFAULT_ID} if it does not exist.
	 */
	protected static int getLocalId(
			Uri contentUri,
			String idColumnName,
			String globalIdColumnName,
			String globalId,
			ContentResolver resolver) {
		int localId = ENTITY_DEFAULT_ID;
		
		Cursor cursor = null;
		try {
			cursor = resolver.query(
					contentUri,
					new String[] { idColumnName },
					globalIdColumnName + " = ?",
					new String[] { globalId },
					null);
			
			if (cursor.moveToFirst())
				localId = getInt(cursor, idColumnName);
		} catch (Exception e) {
			Log.e(TAG, e.getMessage(), e);
		} finally {
			if (cursor != null)
				cursor.close();
		}
		
		return localId;
	}
	
	/**
	 * Gets the global ID of the entity corresponding to the specified content URI and
	 * local ID. 
	 * @param contentUri The content URL.
	 * @param localId The local ID of the entity.
	 * @param globalIdColumnName The name of the global ID column.
	 * @param resolver The content resolver.
	 * @return The global ID of the specified entity, or <code>null</code> if it does
	 * not exist.
	 */
	protected static String getGlobalId(
			Uri contentUri, long localId, String globalIdColumnName, ContentResolver resolver) {
		String globalId = null;
		
		Cursor cursor = null;
		try {
			cursor = resolver.query(
					ContentUris.withAppendedId(contentUri, localId),
					new String[] { globalIdColumnName },
					null,
					null,
					null);
			
			if (cursor.moveToNext())
				globalId = Entity.getString(cursor, globalIdColumnName);
		} catch (Exception e) {
			Log.e(TAG, e.getMessage(), e);
		} finally {
			if (cursor != null)
				cursor.close();
		}
		
		return globalId;
	}
	
	/**
	 * Gets the value of the specified column as a string.
	 * @param cursor The database cursor.
	 * @param columnName The name of the column.
	 * @return The value of the specified column as a string.
	 * @throws IllegalArgumentException If the specified column does not exist.
	 */
	protected static String getString(Cursor cursor, String columnName)
			throws IllegalArgumentException {
		return cursor.getString(cursor.getColumnIndexOrThrow(columnName));
	}
	
	/**
	 * Gets the value of the specified column as an integer.
	 * @param cursor The database cursor.
	 * @param columnName The name of the column.
	 * @return The value of the specified column as an integer.
	 * @throws IllegalArgumentException If the specified column does not exist.
	 */
	protected static int getInt(Cursor cursor, String columnName)
			throws IllegalArgumentException {
		return cursor.getInt(cursor.getColumnIndexOrThrow(columnName));
	}
	
	/**
	 * Gets the value of the specified column as a boolean.
	 * @param cursor The database cursor.
	 * @param columnName The name of the column.
	 * @return The value of the specified column as a boolean.
	 * @throws IllegalArgumentException If the specified column does not exist.
	 */
	protected static boolean getBoolean(Cursor cursor, String columnName)
			throws IllegalArgumentException {
		return getInt(cursor, columnName) == 1;
	}
	
	/**
	 * Gets the value of the specified column as a long.
	 * @param cursor The database cursor.
	 * @param columnName The name of the column.
	 * @return The value of the specified column as a long.
	 * @throws IllegalArgumentException If the specified column does not exist.
	 */
	protected static long getLong(Cursor cursor, String columnName)
			throws IllegalArgumentException {
		return cursor.getLong(cursor.getColumnIndexOrThrow(columnName));
	}
	
	/**
	 * Populates the entity with the values from the specified cursor.
	 * @param cursor The database cursor.
	 */
	protected abstract void populate(Cursor cursor);
	
	/**
	 * Gets the values of the entity.
	 * @return A mapping between property name and value.
	 */
	protected abstract ContentValues getEntityValues();
	
	/**
	 * Gets the content URL of the entity.
	 * @return The content URL of the entity.
	 */
	protected abstract Uri getContentUri();
	
	/**
	 * Inserts the entity into the database.
	 * @param resolver The content resolver.
	 * @return The URL to the newly inserted entity.
	 * @throws IllegalStateException If the entity is already in the database.
	 */
	public Uri insert(ContentResolver resolver) throws IllegalStateException {
		if (getId() == ENTITY_DEFAULT_ID)
			return resolver.insert(getContentUri(), getEntityValues());
		else
			throw new IllegalStateException("The entity is already in the database.");
	}
	
	/**
	 * Updates the entity in the database.
	 * @param resolver The content resolver.
	 * @return The number of rows updated.
	 * @throws IllegalStateException If the entity is not in the database.
	 */
	public int update(ContentResolver resolver) throws IllegalStateException {
		if (getId() != ENTITY_DEFAULT_ID) {
			Uri contentUri = ContentUris.withAppendedId(getContentUri(), getId());
			
			return resolver.update(contentUri, getEntityValues(), null, null);
		} else {
			throw new IllegalStateException("The entity is not in the database.");
		}
	}
	
	/**
	 * Removes the entity from the database.
	 * @param resolver The content resolver.
	 * @return The number of rows deleted.
	 * @throws IllegalStateException If the entity is not in the database.
	 */
	public int delete(ContentResolver resolver) throws IllegalStateException {
		if (getId() != ENTITY_DEFAULT_ID) {
			Uri contentUri = ContentUris.withAppendedId(getContentUri(), getId());
			
			return resolver.delete(contentUri, null, null);
		} else {
			throw new IllegalStateException("The entity is not in the database.");
		}
	}
	
	/**
	 * Serializes the entity into a string.
	 * @return A string representation of the entity, or <code>null</code> if the
	 * serialization fails.
	 */
	public String serialize() {
		int localId = getId();
		setId(ENTITY_DEFAULT_ID); /* Prevent actual local ID from being serialized */
		
		String serialized = null;
		try {
			Gson serializer = new Gson();
			serialized = serializer.toJson(this);
		} catch (Exception e) {
			Log.e(TAG, e.getMessage(), e);
		} finally {
			setId(localId); /* Restore local ID */
		}
		
		return serialized;
	}
	
	/**
	 * Parses a serialized entity into an object.
	 * @param serialized The serialized entity.
	 * @param entityClass The entity class to parse into.
	 * @return The parsed entity, or <code>null</code> if the deserialization
	 * fails.
	 */
	public static <T extends Entity> T deserialize(
			String serialized, Class<T> entityClass) {
		T entity = null;
		try {
			Gson serializer = new Gson();
			entity = (T) serializer.fromJson(serialized, entityClass);
		} catch (Exception e) {
			Log.e(TAG, e.getMessage(), e);
		}
		
		return entity;
	}
	
	/**
	 * Gets the local ID of the entity.
	 * @return The local ID of the entity.
	 */
	public abstract int getId();
	
	/**
	 * Sets the local ID of the entity.
	 * @param id The local ID of the entity.
	 */
	protected abstract void setId(int id);
	
	/**
	 * Fetches the local ID of the entity from the database. If the entity
	 * does not exist in the database, the local ID is set to
	 * {@link Entity#ENTITY_DEFAULT_ID}.
	 * @param resolver The content resolver.
	 */
	public abstract void fetchLocalId(ContentResolver resolver);
	
	/**
	 * Gets the global ID of the entity.
	 * @return The global ID of the entity.
	 */
	public abstract String getGlobalId();
	
	/**
	 * Sets the global ID of the entity.
	 * @param globalId The global ID of the entity.
	 */
	public abstract void setGlobalId(String globalId);
}
