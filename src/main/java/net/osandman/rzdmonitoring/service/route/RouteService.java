package net.osandman.rzdmonitoring.service.route;

import net.osandman.rzdmonitoring.dto.route.RoutesResult;

/**
 * Сервис для поиска маршрутов.
 */
public interface RouteService {

    /**
     * Поиск маршрута.
     */
    RoutesResult findRoutes(String fromStationCode, String toStationCode, String departureDate);

    String getRoutesAsString(String fromStationCode, String toStationCode, String departureDate);
}
