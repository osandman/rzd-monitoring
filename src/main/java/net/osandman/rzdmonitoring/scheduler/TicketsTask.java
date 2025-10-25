package net.osandman.rzdmonitoring.scheduler;

import lombok.Builder;
import org.springframework.lang.NonNull;

import java.util.List;
import java.util.Set;

import static net.osandman.rzdmonitoring.config.Constant.DATE_FORMAT_PATTERN;
import static net.osandman.rzdmonitoring.util.Utils.convertDateString;

@Builder
public record TicketsTask(
    Long chatId,
    String taskId,
    String date,
    String fromCode,
    String fromStation,
    String toCode,
    String toStation,
    List<String> routeNumbers,
    Set<String> filters
) {

    @NonNull
    public String prettyString() {
        String formattedDate = convertDateString(date, DATE_FORMAT_PATTERN);
        return fromStation + " - " + toStation + " " + formattedDate
               + " №" + routeNumbers + " фильтры:" + filters;
    }

    @Override
    @NonNull
    public String toString() {
        return "TicketsTask{" +
               "chatId=" + chatId +
               ", taskId=" + taskId +
               ", date=" + date +
               ", fromCode=" + fromCode +
               ", toCode=" + toCode +
               ", routeNumbers=" + routeNumbers +
               '}';
    }
}
