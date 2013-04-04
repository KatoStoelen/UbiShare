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
package org.societies.android.p2p.entity;

import java.util.ArrayList;
import java.util.Collection;

import org.societies.android.platform.entity.Entity;

import com.google.renamedgson.Gson;
import com.google.renamedgson.GsonBuilder;

/**
 * A response entity used to send updates to clients.
 * 
 * @author Kato
 */
public class Response {
	
	private Collection<Entity> mEntities = new ArrayList<Entity>();
	
	/**
	 * Serializes the response to a string.
	 * @return A string representing the serialized response.
	 */
	public String serialize() {
		Gson serializer = new GsonBuilder()
			.registerTypeAdapter(Response.class, new ResponseTypeAdapter())
			.create();
		
		return serializer.toJson(this, Response.class);
	}
	
	/**
	 * Deserializes the specified string into a response.
	 * @param serialized The serialized response.
	 * @return The deserialized response.
	 */
	public static Response deserialize(String serialized) {
		Gson serializer = new GsonBuilder()
			.registerTypeAdapter(Response.class, new ResponseTypeAdapter())
			.create();
		
		return serializer.fromJson(serialized, Response.class);
	}

	/**
	 * Gets the list of entities.
	 * @return The list of entities.
	 */
	public Collection<Entity> getEntities() {
		return mEntities;
	}

	/**
	 * Sets the list of entities.
	 * @param entities The list of entities.
	 */
	public void setEntities(Collection<Entity> entities) {
		mEntities = entities;
	}
}
