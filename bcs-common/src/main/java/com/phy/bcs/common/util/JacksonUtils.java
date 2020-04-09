package com.phy.bcs.common.util;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.IdentityHashMap;
import java.util.Map;

/**
 * jackson json工具类
 *
 * @author lijie
 */
public class JacksonUtils {

    static ObjectMapper mapper = new ObjectMapper();

    static {
        mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        mapper.enable(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY);
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
    }

    public static Map<String, Object> linkedHashMapToObj(Map<String, Object> factObjects) {
        Map<String, Object> factObjectsResult = new IdentityHashMap<String, Object>();
        try {
            for (Map.Entry<String, Object> factObject : factObjects.entrySet()) {
                String factName = factObject.getKey();
                Class factObjectClass = Class.forName(factName);
                Object fact = factObject.getValue();

                ObjectMapper mapper = new ObjectMapper();
                Object object = mapper.convertValue(fact, factObjectClass);
                factObjectsResult.put(factName, object);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return factObjectsResult;
    }

    public static ObjectMapper getMapper() {
        return mapper;
    }

    public static String serializeObject(Object o) throws IOException {
        return mapper.writeValueAsString(o);
    }

    public static String toJsonString(Object o) {
        try {
            return serializeObject(o);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            return o.toString();
        }
    }

    public static Object deserializeObject(String s, Class<?> clazz) throws IOException {
        return mapper.readValue(s, clazz);
    }

    public static Object deserializeObject(String s, TypeReference<?> typeReference)
            throws IOException {
        return mapper.readValue(s, typeReference);
    }

    public static JavaType getCollectionType(Class<?> collectionClass, Class<?>... elementClasses) {
        return mapper.getTypeFactory().constructParametricType(collectionClass, elementClasses);
    }

    public static Object deserializeCollection(String s, JavaType type) throws IOException {
        return mapper.readValue(s, type);
    }

}
