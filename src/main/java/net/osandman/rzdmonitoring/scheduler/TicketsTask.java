package net.osandman.rzdmonitoring.scheduler;

import lombok.Builder;
import org.springframework.lang.NonNull;

import java.util.Arrays;

@Builder
public record TicketsTask(
    Long chatId, String taskId, String date, String fromCode, String toCode, String... routeNumbers
) {

    @Override
    @NonNull
    public String toString() {
        return "TicketsTask{" +
               "chatId=" + chatId +
               ", taskId=" + taskId +
               ", date=" + date +
               ", fromCode=" + fromCode +
               ", toCode=" + toCode +
               ", routeNumbers=" + Arrays.toString(routeNumbers) +
               '}';
    }
}
