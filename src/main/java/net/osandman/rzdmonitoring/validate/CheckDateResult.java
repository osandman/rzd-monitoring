package net.osandman.rzdmonitoring.validate;

import java.time.LocalDate;

public record CheckDateResult(boolean valid, String message, LocalDate localDate) {
}
