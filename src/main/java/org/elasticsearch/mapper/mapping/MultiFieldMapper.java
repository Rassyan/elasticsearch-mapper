package org.elasticsearch.mapper.mapping;

import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.mapper.annotations.fieldtype.MultiField;
import org.elasticsearch.mapper.annotations.fieldtype.MultiNestedField;
import org.elasticsearch.mapper.annotations.fieldtype.TokenCountField;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Set;

public class MultiFieldMapper {

    public static boolean isValidMultiFieldType(Field field) {
        Class<?> fieldClass = field.getType();
        return String.class.isAssignableFrom(fieldClass) && field.isAnnotationPresent(MultiField.class);
    }

    public static void mapDataType(XContentBuilder mappingBuilder, Field field, Set<String> analyzers) throws IOException {
        if (!isValidMultiFieldType(field)) {
            throw new IllegalArgumentException(
                    String.format("field type[%s] is invalid type of string.", field.getType()));
        }
        MultiField multiField = field.getDeclaredAnnotation(MultiField.class);
        StringFieldMapper.mapDataType(mappingBuilder, multiField.mainField(), analyzers);

        mappingBuilder.startObject("fields");

        for (MultiNestedField otherField : multiField.fields()) {
            mappingBuilder.startObject(otherField.name());
            StringFieldMapper.mapDataType(mappingBuilder, otherField.field(), analyzers);
            mappingBuilder.endObject();
        }

        for (TokenCountField tokenCountField : multiField.tokenFields()) {
            mappingBuilder.startObject(tokenCountField.name());
            TokenCountFieldMapper.mapDataType(mappingBuilder, tokenCountField);
            mappingBuilder.endObject();
        }

        mappingBuilder.endObject();
    }
}
