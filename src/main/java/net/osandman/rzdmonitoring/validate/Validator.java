package net.osandman.rzdmonitoring.validate;

import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

import static net.osandman.rzdmonitoring.util.Utils.parseDate;

@Component
public class Validator {

    private static final long maxDaysAllowToBuy = 120L;

    public CheckDateResult dateValidate(String dateStr) {
        try {
            LocalDate localDate = parseDate(dateStr);
            return dateValidate(localDate);
        } catch (Exception e) {
            return new CheckDateResult(
                false, "Некорректный формат даты '%s', введите заново".formatted(dateStr), null
            );
        }
    }

    public CheckDateResult dateValidate(@NonNull LocalDate localDate) {
        boolean valid = true;
        String message = "ОК";
        if (localDate.isBefore(LocalDate.now())) {
            message = "Дата меньше текущей, введите заново";
            valid = false;
        }
        if (localDate.isAfter(LocalDate.now().plusDays(maxDaysAllowToBuy))) {
            message = "Дата превышает %d дней от текущей, введите заново".formatted(maxDaysAllowToBuy);
            valid = false;
        }
        return new CheckDateResult(valid, message, localDate);
    }
}
