package net.osandman.rzdmonitoring.dto.train;

import lombok.Builder;

import java.util.List;

@Builder
public record TicketsResult(
    int successTrainCount,
    String comment,
    List<TrainDto> trains
) {
}
