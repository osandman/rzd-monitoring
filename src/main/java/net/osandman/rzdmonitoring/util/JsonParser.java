package net.osandman.rzdmonitoring.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.MappingIterator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.SerializationFeature;
import lombok.experimental.UtilityClass;
import lombok.extern.log4j.Log4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

@UtilityClass
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
        List<T> resultList = new ArrayList<>();
        try (MappingIterator<T> mappingIterator = reader.readValues(jsonString)) {
            resultList = mappingIterator.readAll();
        } catch (IOException | IllegalArgumentException e) {
            logger.error("Invalid read value from JSON, {}", e.getMessage());
        }
        return resultList;
    }
}
