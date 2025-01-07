package net.osandman.rzdmonitoring.util;

import java.util.regex.Pattern;

public final class Utils {

    public static String detectLang(String text) {
        return Pattern.matches(".*\\p{InCYRILLIC}.*", text) ? "ru" : "en";
    }

    public static void sleep(int millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
