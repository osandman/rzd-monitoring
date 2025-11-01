package net.osandman.rzdmonitoring.util;

import java.security.SecureRandom;
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

    /**
     * Генерирует ID: 4 символа timestamp + 4 случайных символа
     * Обеспечивает временную упорядоченность
     * Примеры: "7A2KB9M2", "7A2KC3X8"
     */
    public static String generateTaskId() {
        // Первые 4 символа - закодированный timestamp
        long timestamp = System.currentTimeMillis() / 1000; // секунды
        String timepart = Long.toString(timestamp, 36).toUpperCase();
        if (timepart.length() > 4) {
            timepart = timepart.substring(timepart.length() - 4);
        }
        while (timepart.length() < 4) {
            timepart = "0" + timepart;
        }

        // Последние 4 символа - случайные
        String CHARS = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        SecureRandom secureRandom = new SecureRandom();
        StringBuilder random = new StringBuilder();
        for (int i = 0; i < 4; i++) {
            random.append(CHARS.charAt(secureRandom.nextInt(CHARS.length())));
        }

        return timepart + random;
    }
}
