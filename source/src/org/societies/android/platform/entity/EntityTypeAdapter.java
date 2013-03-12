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
package org.societies.android.platform.entity;

import java.lang.reflect.Type;

import com.google.renamedgson.JsonDeserializationContext;
import com.google.renamedgson.JsonDeserializer;
import com.google.renamedgson.JsonElement;
import com.google.renamedgson.JsonObject;
import com.google.renamedgson.JsonParseException;
import com.google.renamedgson.JsonPrimitive;
import com.google.renamedgson.JsonSerializationContext;
import com.google.renamedgson.JsonSerializer;

/**
 * Type adapter used to serialize entities.
 * 
 * @author Kato
 */
public class EntityTypeAdapter implements
	JsonSerializer<Entity>, JsonDeserializer<Entity> {
	
	private static final String CLASSNAME = "class_name";
	private static final String ENTITY = "entity";

	/*
	 * (non-Javadoc)
	 * @see com.google.renamedgson.JsonDeserializer#deserialize(com.google.renamedgson.JsonElement, java.lang.reflect.Type, com.google.renamedgson.JsonDeserializationContext)
	 */
	public Entity deserialize(
			JsonElement json,
			Type type,
			JsonDeserializationContext context
	) throws JsonParseException {
		JsonObject jsonObject = json.getAsJsonObject();
		
		JsonPrimitive classPrimitive = (JsonPrimitive) jsonObject.get(CLASSNAME);
		String className = classPrimitive.getAsString();
		
		Class<?> clazz = null;
		try {
			clazz = Class.forName(className);
		} catch (ClassNotFoundException e) {
			throw new JsonParseException(e.getMessage());
		}
		
		return context.deserialize(jsonObject.get(ENTITY), clazz);
	}

	/*
	 * (non-Javadoc)
	 * @see com.google.renamedgson.JsonSerializer#serialize(java.lang.Object, java.lang.reflect.Type, com.google.renamedgson.JsonSerializationContext)
	 */
	public JsonElement serialize(
			Entity entity, Type type, JsonSerializationContext context) {
		JsonObject jsonObject = new JsonObject();
		
		String className = entity.getClass().getCanonicalName();
		jsonObject.addProperty(CLASSNAME, className);
		
		JsonElement jsonElement = context.serialize(entity);
		jsonObject.add(ENTITY, jsonElement);
		
		return jsonObject;
	}

}
