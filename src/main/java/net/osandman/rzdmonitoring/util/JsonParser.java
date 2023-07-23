package net.osandman.rzdmonitoring.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;

public class JsonParser {
    private static final Logger logger = LoggerFactory.getLogger(JsonParser.class);
    private static final ObjectMapper objectMapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);

    public static <T> T parse(InputStream json, Class<T> clazz) {
        T root = null;
        try {
            root = objectMapper.readValue(json, clazz);
        } catch (IOException e) {
            logger.error("An error has occurred {}", e.getMessage());
        }
        return root;
    }

    public static <T> T parse(String json, Class<T> clazz) {
        T root = null;
        try {
            root = objectMapper.readValue(json, clazz);
        } catch (JsonProcessingException e) {
            logger.error("An error has occurred {}", e.getMessage());
        }
        return root;
    }
}
