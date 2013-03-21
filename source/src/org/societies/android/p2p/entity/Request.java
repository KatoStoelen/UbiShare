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
 * A request entity.
 * 
 * @author Kato
 */
public class Request {
	
	/**
	 * An enum containing types of requests.
	 */
	public enum RequestType {
		/** Indicates a request to fetch all data. */
		FETCH_ALL,
		
		/** Indicates a request to fetch updated data. */
		FETCH_UPDATES,
		
		/** Indicates a request to push data from the client and fetch updated data. */
		PUSH_AND_FETCH
	}
	
	private RequestType mType;
	private long mLastRequest;
	private Collection<Entity> mUpdatedEntities = new ArrayList<Entity>();
	
	/**
	 * Default constructor.
	 */
	public Request() { /* Empty Default Constructor */ }
	
	/**
	 * Initializes a new request of the specified type.
	 * @param type The type of request.
	 * @param lastRequest The timestamp of the last request (UNIX time
	 * in seconds).
	 */
	public Request(RequestType type, long lastRequest) {
		setType(type);
		setLastRequestTime(lastRequest);
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
		this.mType = type;
	}

	/**
	 * Gets the timestamp of the last request (UNIX time in seconds).
	 * @return The timestamp of the last request.
	 */
	public long getLastRequestTime() {
		return mLastRequest;
	}

	/**
	 * Sets the timestamp of the last request.
	 * @param lastRequest The timestamp of the last request (UNIX time
	 * in seconds).
	 */
	public void setLastRequestTime(long lastRequest) {
		this.mLastRequest = lastRequest;
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
}
