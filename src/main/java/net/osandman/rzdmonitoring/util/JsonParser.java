package net.osandman.rzdmonitoring.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import net.osandman.rzdmonitoring.dto.route.RootRoute;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.stream.Collectors;

public class JsonParser {
    private static final ObjectMapper objectMapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);

    public static <T> T parse(InputStream json, Class<T> clazz) {
        T root;
        try {
            root = objectMapper.readValue(json, clazz);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return root;
    }

    public static <T> T parse(String json, Class<T> clazz) {
        T root;
        try {
            root = objectMapper.readValue(json, clazz);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        return root;
    }
}
