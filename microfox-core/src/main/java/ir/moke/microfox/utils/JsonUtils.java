package ir.moke.microfox.utils;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.TreeNode;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.databind.type.CollectionType;
import com.fasterxml.jackson.databind.type.MapType;
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule;
import ir.moke.microfox.exception.MicrofoxException;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class JsonUtils {

    private static final ObjectMapper objectMapper;

    static {
        objectMapper = JsonMapper.builder()
                .configure(JsonParser.Feature.INCLUDE_SOURCE_IN_LOCATION, true)
                .configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true)
                .configure(DeserializationFeature.USE_JAVA_ARRAY_FOR_JSON_ARRAY, true)
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                .configure(DeserializationFeature.FAIL_ON_IGNORED_PROPERTIES, false)
                .configure(DeserializationFeature.FAIL_ON_IGNORED_PROPERTIES, false)
                .configure(DeserializationFeature.READ_UNKNOWN_ENUM_VALUES_AS_NULL, true)
                .build()
                .registerModule(new ParameterNamesModule(JsonCreator.Mode.PROPERTIES))
                .setSerializationInclusion(JsonInclude.Include.NON_NULL);
    }

    public static String toJson(Object o) {
        try {
            return objectMapper.writeValueAsString(o);
        } catch (JsonProcessingException e) {
            throw new MicrofoxException(e);
        }
    }

    public static <T> T toObject(String str, Class<T> clazz) {
        try {
            return objectMapper.readValue(str, clazz);
        } catch (JsonProcessingException e) {
            throw new MicrofoxException(e);
        }
    }

    public static <T> T toObject(byte[] bytes, Class<T> clazz) {
        return toObject(new String(bytes), clazz);
    }

    public static <T> T toObject(File file, Class<T> clazz) {
        try {
            return objectMapper.readValue(file, clazz);
        } catch (IOException e) {
            throw new MicrofoxException(e);
        }
    }

    public static <T> List<Object> toList(String str) {
        try {
            TypeReference<List<Object>> typeRef = new TypeReference<>() {
            };
            return objectMapper.readValue(str, typeRef);
        } catch (IOException e) {
            throw new MicrofoxException(e);
        }

    }

    public static <T> T toObject(String str, Class<? extends Collection> collectionType, Class<?> genericType) {
        try {
            CollectionType listType = objectMapper.getTypeFactory().constructCollectionType(collectionType, genericType);
            return objectMapper.readValue(str, listType);
        } catch (IOException e) {
            throw new MicrofoxException(e);
        }
    }

    public static <T> T toObject(String str, String canonicalType) {
        try {
            JavaType javaType = objectMapper.getTypeFactory().constructFromCanonical(canonicalType);
            return objectMapper.readValue(str, javaType);
        } catch (IOException e) {
            throw new MicrofoxException(e);
        }
    }

    public static <T> HashMap<String, T> toMap(String str, Class<? extends Map<String, T>> mapClassType, Class<T> genericType) {
        try {
            MapType mapType = objectMapper.getTypeFactory().constructMapType(mapClassType, String.class, genericType);
            return objectMapper.readValue(str, mapType);
        } catch (IOException e) {
            throw new MicrofoxException(e);
        }
    }

    public static <T> Map<String, T> toMap(String str) {
        try {
            TypeReference<HashMap<String, T>> typeRef = new TypeReference<>() {
            };
            return objectMapper.readValue(str, typeRef);
        } catch (IOException e) {
            throw new MicrofoxException(e);
        }
    }

    public static void writeToFile(File file, Object object) {
        try {
            objectMapper.writeValue(file, object);
        } catch (IOException e) {
            throw new MicrofoxException(e);
        }
    }

    public static boolean isJson(String str) {
        try {
            objectMapper.readTree(str);
            return true;
        } catch (JsonProcessingException e) {
            return false;
        }
    }

    public static ObjectMapper getObjectMapper() {
        return objectMapper;
    }

    public static <T> T convert(Object o, Class<T> clazz) {
        return objectMapper.convertValue(o, clazz);
    }

    public static JsonNode readTree(String str) {
        try {
            return objectMapper.readTree(str);
        } catch (JsonProcessingException e) {
            throw new MicrofoxException(e);
        }
    }

    public static <T> T treeToValue(TreeNode treeNode, Class<T> clazz) {
        try {
            return objectMapper.treeToValue(treeNode, clazz);
        } catch (JsonProcessingException e) {
            throw new MicrofoxException(e);
        }
    }
}
