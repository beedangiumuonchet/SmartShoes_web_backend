package com.ds.project.app_context.converters;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class FloatArrayConverter implements AttributeConverter<float[], String> {

    @Override
    public String convertToDatabaseColumn(float[] attribute) {
        if (attribute == null || attribute.length == 0) return null;

        // Format PostgreSQL vector: "1.2, 3.4, 5.6"
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < attribute.length; i++) {
            sb.append(attribute[i]);
            if (i < attribute.length - 1) sb.append(", ");
        }
        return sb.toString();
    }

    @Override
    public float[] convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.isEmpty()) return new float[0];

        String[] parts = dbData.split(",");
        float[] result = new float[parts.length];
        for (int i = 0; i < parts.length; i++) {
            result[i] = Float.parseFloat(parts[i].trim());
        }
        return result;
    }
}

