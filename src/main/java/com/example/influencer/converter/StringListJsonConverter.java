package com.example.influencer.converter;

import java.util.List;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import tools.jackson.core.JacksonException;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

@Converter
public class StringListJsonConverter implements AttributeConverter<List<String>, String> {

	private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
	private static final TypeReference<List<String>> LIST_TYPE = new TypeReference<>() {
	};

	@Override
	public String convertToDatabaseColumn(List<String> attribute) {
		if (attribute == null) {
			return null;
		}
		try {
			return OBJECT_MAPPER.writeValueAsString(attribute);
		} catch (JacksonException ex) {
			throw new IllegalArgumentException("Unable to convert categories to JSON", ex);
		}
	}

	@Override
	public List<String> convertToEntityAttribute(String dbData) {
		if (dbData == null || dbData.isBlank()) {
			return null;
		}
		try {
			return OBJECT_MAPPER.readValue(dbData, LIST_TYPE);
		} catch (JacksonException ex) {
			throw new IllegalArgumentException("Unable to convert categories from JSON", ex);
		}
	}
}
