package com.narangnorang.space.entity.converter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter
public class CustomFieldConverter implements AttributeConverter<Map<String, List<String>>, String> {

    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static final TypeReference<Map<String, List<String>>> TYPE_REF = new TypeReference<>() {};

    @Override
    public String convertToDatabaseColumn(Map<String, List<String>> attribute) {
        if (attribute == null) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(attribute);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("customField JSON 변환 실패", e);
        }
    }

    @Override
    public Map<String, List<String>> convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.isBlank()) {
            return new HashMap<>();
        }
        try {
            return objectMapper.readValue(dbData, TYPE_REF);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("customField JSON 파싱 실패", e);
        }
    }
}