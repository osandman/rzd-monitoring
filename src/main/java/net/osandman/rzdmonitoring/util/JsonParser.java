package net.osandman.rzdmonitoring.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

public class JsonParser {
    private static final Logger logger = LoggerFactory.getLogger(JsonParser.class);
    private static final ObjectMapper objectMapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);

    public static <T> T parse(InputStream json, Class<T> clazz) {
        T result = null;
        try {
            result = objectMapper.readValue(json, clazz);
        } catch (IOException e) {
            logger.error("Invalid read value from JSON, {}", e.getMessage());
        }
        return result;
    }

    public static <T> T parse(String jsonString, Class<T> clazz) {
        T result = null;
        try {
            result = objectMapper.readValue(jsonString, clazz);
        } catch (JsonProcessingException e) {
            logger.error("Invalid read value from JSON, {}", e.getMessage());
        }
        return result;
    }

    public static <T> List<T> parseValues(String jsonString, Class<T> clazz) {
        ObjectReader reader = objectMapper.readerFor(clazz);
        List<T> resultList = null;
        try {
            resultList = reader.<T>readValues(jsonString).readAll();
        } catch (IOException e) {
            logger.error("Invalid read value from JSON, {}", e.getMessage());
        }
        return resultList;
    }
}
