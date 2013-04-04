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

import java.lang.reflect.Type;
import java.util.Collection;

import org.societies.android.platform.entity.Entity;
import org.societies.android.platform.entity.EntityTypeAdapter;

import com.google.renamedgson.Gson;
import com.google.renamedgson.GsonBuilder;
import com.google.renamedgson.JsonDeserializationContext;
import com.google.renamedgson.JsonDeserializer;
import com.google.renamedgson.JsonElement;
import com.google.renamedgson.JsonObject;
import com.google.renamedgson.JsonParseException;
import com.google.renamedgson.JsonPrimitive;
import com.google.renamedgson.JsonSerializationContext;
import com.google.renamedgson.JsonSerializer;
import com.google.renamedgson.reflect.TypeToken;

/**
 * Type adapter used to serialize responses.
 * 
 * @author Kato
 */
public class ResponseTypeAdapter implements
	JsonSerializer<Response>, JsonDeserializer<Response> {

	private static final String PROP_ENTITIES = "entities";
	
	/* (non-Javadoc)
	 * @see com.google.renamedgson.JsonSerializer#serialize(java.lang.Object, java.lang.reflect.Type, com.google.renamedgson.JsonSerializationContext)
	 */
	public JsonElement serialize(
			Response response, Type type, JsonSerializationContext context) {
		Gson serializer = new GsonBuilder()
			.registerTypeAdapter(Entity.class, new EntityTypeAdapter())
			.create();
	
		Type collectionType = new TypeToken<Collection<Entity>>() {}.getType();
	
		String serializedEntities = serializer.toJson(
				response.getEntities(), collectionType);
		
		JsonObject jsonObject = new JsonObject();
		jsonObject.addProperty(PROP_ENTITIES, serializedEntities);
		
		return jsonObject;
	}
	
	/* (non-Javadoc)
	 * @see com.google.renamedgson.JsonDeserializer#deserialize(com.google.renamedgson.JsonElement, java.lang.reflect.Type, com.google.renamedgson.JsonDeserializationContext)
	 */
	public Response deserialize(
			JsonElement json, Type type, JsonDeserializationContext context
	) throws JsonParseException {
		JsonObject jsonObject = json.getAsJsonObject();
		
		JsonPrimitive entitiesPrimitive = (JsonPrimitive) jsonObject.get(PROP_ENTITIES);
		
		Gson serializer = new GsonBuilder()
			.registerTypeAdapter(Entity.class, new EntityTypeAdapter())
			.create();
		
		Type collectionType = new TypeToken<Collection<Entity>>() {}.getType();
		
		Collection<Entity> entities = serializer.fromJson(
				entitiesPrimitive.getAsString(), collectionType);
		
		Response response = new Response();
		response.setEntities(entities);
		
		return response;
	}
}
