package org.elasticsearch.mapper.mapping;


import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.mapper.annotations.Document;
import org.elasticsearch.mapper.annotations.IgnoreField;
import org.elasticsearch.mapper.utils.BeanUtils;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.HashSet;
import java.util.Set;

public class MappingBuilder {
    protected final String DEFAULT_TYPE_NAME = "_doc";

    public Set<String> buildMapping(Class<?> documentClazz, XContentBuilder mappingBuilder) throws IOException {
        if (documentClazz == null) {
            throw new IllegalArgumentException("param[documentClazz] can not be null!");
        }

        if (!documentClazz.isAnnotationPresent(Document.class)) {
            throw new IllegalArgumentException(
                    String.format("Can't find annotation[@Document] at class[%s]", documentClazz.getName()));
        }

//        mappingBuilder.startObject();
        Document document = documentClazz.getAnnotation(Document.class);

        if (document == null) {
            throw new IllegalStateException(
                    String.format("Can't find annotation[@Document] at class[%s]", documentClazz.getName()));
        }

        mappingBuilder.startObject(DEFAULT_TYPE_NAME);

        buildTypeSetting(mappingBuilder, documentClazz);
        Set<String> analyzers = new HashSet<>();
        buildTypeProperty(mappingBuilder, documentClazz, analyzers);

        mappingBuilder.endObject();
//        mappingBuilder.endObject();
        return analyzers;
    }

    private void buildTypeSetting(XContentBuilder mapping, Class clazz) throws IOException {
        Document document = (Document) clazz.getAnnotation(Document.class);

        if (!document._all().enabled()) {
            mapping.startObject("_all").field("enabled", false).endObject();
        }

        if (document._routing().required()) {
            mapping.startObject("_routing").field("required", true).endObject();
        }

        if (!document.dynamic()) {
            mapping.field("dynamic", false);
        }
    }

    private XContentBuilder buildTypeProperty(XContentBuilder mappingBuilder, Class clazz, Set<String> analyzers) throws IOException {
        mappingBuilder.startObject("properties");

        Field[] classFields = BeanUtils.retrieveFields(clazz);
        for (Field classField : classFields) {
            String fieldName = classField.getName();

            if (Modifier.isTransient(classField.getModifiers())
                    || Modifier.isStatic(classField.getModifiers())
                    || fieldName.equals("$VRc") || fieldName.equals("serialVersionUID")) {
                continue;
            }

            if (classField.getAnnotation(IgnoreField.class) != null) {
                continue;
            }

            buildFieldProperty(mappingBuilder, classField, analyzers);
        }

        mappingBuilder.endObject();

        return mappingBuilder;
    }

    private XContentBuilder buildFieldProperty(XContentBuilder mappingBuilder, Field field, Set<String> analyzers) throws IOException {
        mappingBuilder.startObject(field.getName());

        // Geo point  field
        if (GeoPointFieldMapper.isValidGeoPointFieldType(field)) {
            GeoPointFieldMapper.mapDataType(mappingBuilder, field);
        }
        // Percolator field
        else if (PercolatorFieldMapper.isValidPercolatorFieldType(field)) {
            PercolatorFieldMapper.mapDataType(mappingBuilder, field);
        }
        // IP field
        else if (IPFieldMapper.isValidIPFieldType(field)) {
            IPFieldMapper.mapDataType(mappingBuilder, field);
        }
        // Range field
        else if (RangeFieldMapper.isValidRangeFieldType(field)) {
            RangeFieldMapper.mapDataType(mappingBuilder, field);
        }
        // Number
        else if (NumericFieldMapper.isValidNumberType(field)) {
            NumericFieldMapper.mapDataType(mappingBuilder, field);
        }
        // Boolean
        else if (BooleanFieldMapper.isValidBooleanType(field)) {
            BooleanFieldMapper.mapDataType(mappingBuilder, field);
        }
        // Binary field
        else if (BinaryFieldMapper.isValidBinaryType(field)) {
            BinaryFieldMapper.mapDataType(mappingBuilder, field);
        }
        // Multi field
        else if (MultiFieldMapper.isValidMultiFieldType(field)) {
            MultiFieldMapper.mapDataType(mappingBuilder, field, analyzers);
        }
        // Completion Field
        else if (CompletionFieldMapper.isValidCompletionFieldType(field)) {
            CompletionFieldMapper.mapDataType(mappingBuilder, field);
        }
        // String field
        else if (StringFieldMapper.isValidStringFieldType(field)) {
            StringFieldMapper.mapDataType(mappingBuilder, field, analyzers);
        }
        // Date field
        else if (DateFieldMapper.isValidDateType(field)) {
            DateFieldMapper.mapDataType(mappingBuilder, field);
        }
        // Collection type field
        else if (BeanUtils.isCollectionType(field)) {
            if (!BeanUtils.isValidCollectionType(field)) {
                throw new IllegalArgumentException(
                        String.format("Unsupported list class type, name[%s].", field.getName()));
            }
            Type genericType = BeanUtils.getCollectionGenericType(field);

            //Nested Doc Type
            mappingBuilder.field("type", "nested");
            buildTypeProperty(mappingBuilder, (Class) genericType, analyzers);
        }
        //Inner Doc Type
        else {
            mappingBuilder.field("type", "object");
            buildTypeProperty(mappingBuilder, field.getType(), analyzers);
        }
        mappingBuilder.endObject();

        return mappingBuilder;
    }

}
