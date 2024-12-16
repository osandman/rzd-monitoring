package net.osandman.rzdmonitoring.validate;

import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

import static net.osandman.rzdmonitoring.bot.command.AbstractTelegramCommand.DATE_FORMAT_PATTERN;

@Component
public class Validator {

    public CheckDateResult dateValidate(String dateStr) {
        boolean valid = true;
        String message = "ОК";
        LocalDate localDate = parseDate(dateStr);
        if (localDate == null) {
            return new CheckDateResult(false, "Некорректный формат даты '%s', введите заново".formatted(dateStr));
        }
        if (localDate.isBefore(LocalDate.now())) {
            message = "Дата меньше текущей, введите заново";
            valid = false;
        }
        long maxDaysAllowToBuy = 120L;
        if (localDate.isAfter(LocalDate.now().plusDays(maxDaysAllowToBuy))) {
            message = "Дата превышает %d дней от текущей, введите заново".formatted(maxDaysAllowToBuy);
            valid = false;
        }
        return new CheckDateResult(valid, message);
    }

    private LocalDate parseDate(String dateStr) {
        try {
            return LocalDate.parse(dateStr, DateTimeFormatter.ofPattern(DATE_FORMAT_PATTERN));
        } catch (DateTimeParseException e) {
            return null;
        }
    }
}
