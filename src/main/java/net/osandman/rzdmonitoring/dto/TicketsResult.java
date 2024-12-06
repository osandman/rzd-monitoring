package net.osandman.rzdmonitoring.dto;

import java.util.List;

public record TicketsResult(
    int findRoutes,
    String comment,
    List<TrainDto> trains
) {
}
