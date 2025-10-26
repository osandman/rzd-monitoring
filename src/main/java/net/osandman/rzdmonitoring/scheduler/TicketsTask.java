package net.osandman.rzdmonitoring.scheduler;

import lombok.Builder;
import org.springframework.lang.NonNull;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Set;

import static net.osandman.rzdmonitoring.config.Constant.DATE_FORMAT_PATTERN_SHORT;
import static net.osandman.rzdmonitoring.util.Utils.convertDateString;

@Builder
public record TicketsTask(
    Long chatId,
    String userName,
    String taskId,
    String date,
    String fromCode,
    String fromStation,
    String toCode,
    String toStation,
    Map<String, LocalDateTime> trainDepartureDateMap,
    Set<String> filters
) {

    @NonNull
    public String prettyString() {
        String formattedDate = convertDateString(date, DATE_FORMAT_PATTERN_SHORT);
        return fromStation + " - " + toStation + " " + formattedDate
               + " №" + trainDepartureDateMap.keySet() + " фильтры:" + filters;
    }

    @Override
    @NonNull
    public String toString() {
        return "TicketsTask{" +
               "taskId=" + taskId +
               ", chatId=" + chatId +
               ", userName=" + userName +
               ", date=" + date +
               ", fromCode=" + fromCode +
               ", toCode=" + toCode +
               ", trainDepartureDateMap=" + trainDepartureDateMap +
               '}';
    }
}
