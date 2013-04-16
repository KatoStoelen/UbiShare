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
 * A request entity used to send updates from the client to
 * the server.
 * 
 * @author Kato
 */
public class Request {
	
	/**
	 * An enum containing types of requests.
	 */
	public enum RequestType {
		/** Indicates a request to notify server of a client's presence. */
		HANDSHAKE,
		
		/** Indicates a request containing updates from the client. */
		UPDATE
	}
	
	private String mUniqueId;
	private RequestType mType;
	private Collection<Entity> mUpdatedEntities = new ArrayList<Entity>();
	
	/**
	 * Default constructor.
	 */
	public Request() { /* Empty Default Constructor */ }
	
	/**
	 * Initializes a new request of the specified type.
	 * @param uniqueId The unique ID of the client.
	 * @param type The type of request.
	 */
	public Request(String uniqueId, RequestType type) {
		mUniqueId = uniqueId;
		mType = type;
	}
	
	/**
	 * Serializes the request.
	 * @return The serialized request as a string.
	 */
	public String serialize() {
		Gson serializer = new GsonBuilder()
			.registerTypeAdapter(Request.class, new RequestTypeAdapter())
			.create();
		
		return serializer.toJson(this, Request.class);
	}
	
	/**
	 * Deserializes the specified request.
	 * @param serialized The serialized request.
	 * @return The deserialized request.
	 */
	public static Request deserialize(String serialized) {
		Gson serializer = new GsonBuilder()
			.registerTypeAdapter(Request.class, new RequestTypeAdapter())
			.create();
		
		return serializer.fromJson(serialized, Request.class);
	}

	/**
	 * Gets the type of the request.
	 * @return The type of the request.
	 */
	public RequestType getType() {
		return mType;
	}
	
	/**
	 * Sets the type of the request.
	 * @param type The type of the request.
	 */
	public void setType(RequestType type) {
		mType = type;
	}

	/**
	 * Gets the updated entities.
	 * @return The updated entities.
	 */
	public Collection<Entity> getUpdatedEntities() {
		return mUpdatedEntities;
	}

	/**
	 * Sets the updated entities.
	 * @param updatedEntities The updated entities.
	 */
	public void setUpdatedEntities(Collection<Entity> updatedEntities) {
		this.mUpdatedEntities = updatedEntities;
	}

	/**
	 * Gets the unique ID of the client.
	 * @return The unique ID of the client.
	 */
	public String getUniqueId() {
		return mUniqueId;
	}
	
	/**
	 * Sets the unique ID of the client.
	 * @param uniqueId The unique ID of the client.
	 */
	public void setUniqueId(String uniqueId) {
		mUniqueId = uniqueId;
	}
}
