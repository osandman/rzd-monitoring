package net.osandman.rzdmonitoring.dto.route;

import lombok.Builder;

import java.util.List;

@Builder
public record RoutesResult(
    int routesCount,
    String comment,
    List<RouteDto> routes,
    String error
) {
}