package com.domotrix.android;

import org.codehaus.jackson.map.ObjectMapper;

import java.io.IOException;
import java.io.StringWriter;

class JSONMapper {

	public static <T> T decode(String jsonData, Class<T> clazz) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.readValue(jsonData, clazz);
	}

	public static String encode(Object obj) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
		StringWriter stringWriter = new StringWriter();
        objectMapper.writeValue(stringWriter, obj);
        return stringWriter.toString();		
	}

}