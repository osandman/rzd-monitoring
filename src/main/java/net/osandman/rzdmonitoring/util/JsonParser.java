package net.osandman.rzdmonitoring.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.MappingIterator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

@UtilityClass
@Slf4j
public class JsonParser {

    private static final ObjectMapper objectMapper = new ObjectMapper()
        .registerModule(new JavaTimeModule())
        .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
        .configure(SerializationFeature.INDENT_OUTPUT, true)
        .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);

    public static <T> T parse(InputStream json, Class<T> clazz) {
        T result = null;
        try {
            result = objectMapper.readValue(json, clazz);
        } catch (Exception e) {
            log.error("Invalid read value from JSON, {}", e.getMessage());
        }
        return result;
    }

    public static <T> T parse(String jsonString, Class<T> clazz) {
        T result = null;
        try {
            result = objectMapper.readValue(jsonString, clazz);
        } catch (Exception e) {
            log.error("Invalid read value from JSON, {}", e.getMessage());
        }
        return result;
    }

    public static <T> List<T> parseValues(String jsonString, Class<T> clazz) {
        ObjectReader reader = objectMapper.readerFor(clazz);
        List<T> resultList = new ArrayList<>();
        try (MappingIterator<T> mappingIterator = reader.readValues(jsonString)) {
            resultList = mappingIterator.readAll();
        } catch (Exception e) {
            log.error("Invalid read value from JSON, {}", e.getMessage());
        }
        return resultList;
    }

    public static JsonNode parse(String jsonString) {
        JsonNode result = null;
        try {
            result = objectMapper.readTree(jsonString);
        } catch (JsonProcessingException e) {
            log.error("Invalid read value from JSON, {}", e.getMessage());
        }
        return result;
    }
}
