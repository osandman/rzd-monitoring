package net.osandman.rzdmonitoring.scheduler;

import lombok.Builder;

@Builder
public record TicketsTask(
    Long chatId, String taskId, String date, String fromCode, String toCode, String... routeNumbers
) {
}
