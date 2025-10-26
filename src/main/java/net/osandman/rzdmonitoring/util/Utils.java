package net.osandman.rzdmonitoring.util;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.regex.Pattern;

public final class Utils {

    public static String detectLang(String text) {
        return Pattern.matches(".*\\p{InCYRILLIC}.*", text) ? "ru" : "en";
    }

    public static String removeBracketsWithContent(String input) {
        return input.replaceAll("\\s*\\(.*?\\)", "").trim();
    }

    public static String getFirstWord(String input) {
        if (input == null || input.trim().isEmpty()) {
            return "";
        }
        return input.trim().split("\\s+")[0];
    }

    public static String convertDateString(String dateStr, String outputFormat) {
        LocalDate localDate;
        try {
            localDate = LocalDate.parse(dateStr, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        } catch (DateTimeParseException ignored) {
            return dateStr;
        }
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(outputFormat);
        return localDate.format(formatter);
    }

    public static String dateToString(LocalDateTime localDateTime, String outputFormat) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(outputFormat);
        return localDateTime.format(formatter);
    }

    public static void sleep(int millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(e);
        }
    }
}
