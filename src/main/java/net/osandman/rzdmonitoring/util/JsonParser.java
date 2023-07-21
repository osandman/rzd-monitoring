package net.osandman.rzdmonitoring.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import java.io.IOException;
import java.io.InputStream;

public class JsonParser {
    private static final ObjectMapper objectMapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);

    public static <T> T parse(InputStream json, Class<T> clazz) {
        T root = null;
        try {
            root = objectMapper.readValue(json, clazz);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return root;
    }
}
