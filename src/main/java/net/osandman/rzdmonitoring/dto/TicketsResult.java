package net.osandman.rzdmonitoring.dto;

import lombok.Builder;

import java.util.List;

@Builder
public record TicketsResult(
    int findRoutes,
    String comment,
    List<TrainDto> trains
) {
}
