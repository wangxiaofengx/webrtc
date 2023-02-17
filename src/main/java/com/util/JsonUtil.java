package com.util;


import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;

public class JsonUtil {

    private static volatile ObjectMapper objectMapper = null;

    public static ObjectMapper getObjectMapper() {
        if (objectMapper != null) {
            return objectMapper;
        }
        synchronized (ObjectMapper.class) {
            if (objectMapper != null) {
                return objectMapper;
            }
            objectMapper = new ObjectMapper();
            objectMapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
            SimpleModule simpleModule = new SimpleModule();
            simpleModule.addSerializer(Long.class, ToStringSerializer.instance);
            simpleModule.addSerializer(Long.TYPE, ToStringSerializer.instance);
            objectMapper.registerModule(simpleModule);
        }
        return objectMapper;
    }

}
