package net.osandman.rzdmonitoring.scheduler;

import lombok.Builder;

@Builder
public record TicketsTask(
    String taskId, String date, String fromCode, String toCode, String... routeNumbers
) {
}
