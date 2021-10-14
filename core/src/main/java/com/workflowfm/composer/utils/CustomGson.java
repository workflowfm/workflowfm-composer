package com.workflowfm.composer.utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class CustomGson {
	private static Gson gson = new GsonBuilder()
//	.registerTypeAdapter(CllTerm.class, new JsonDeserializer<CllTerm>() {
//		@Override
//		public CllTerm deserialize(JsonElement arg0, Type arg1,
//				JsonDeserializationContext arg2)
//						throws JsonParseException {
//			// The CLL term object simply stores the JSON object as this
//			// is used as
//			// the object's internal representation
//			return new CllTerm(arg0.getAsJsonObject());
//		}
//	})
//	.registerTypeAdapter(CllTerm.class, new JsonSerializer<CllTerm>() {
//		@Override
//		public JsonElement serialize(CllTerm arg0, Type arg1,
//				JsonSerializationContext arg2) {
//			return arg0.getJson();
//		}
//	})
	.create();

	public static Gson getGson() {
		return gson;
	}

//	@SuppressWarnings("unused")
//	private static <T> T getObject(String json, Class<T> className) {
//		return getGson().fromJson(
//				new JsonParser().parse(json).getAsJsonObject(), className);
//	}
//
//	public static <T> T getObject(String json, String jsonObjectName,
//			Class<T> className) {
//		return getGson().fromJson(
//				new JsonParser().parse(json).getAsJsonObject()
//				.get(jsonObjectName), className);
//	}
}
