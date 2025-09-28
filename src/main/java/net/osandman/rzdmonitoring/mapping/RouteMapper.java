package net.osandman.rzdmonitoring.mapping;

import net.osandman.rzdmonitoring.client.dto.v2.route.RootRouteDto;
import net.osandman.rzdmonitoring.dto.route.RouteDto;

import java.util.List;

/**
 * Маппер для преобразования ответа сервера в RouteDto
 */
public interface RouteMapper {

    /**
     * Преобразовать ответа сервера в список RouteDto
     */
    List<RouteDto> toRoutes(RootRouteDto rootRouteDto);

    /**
     * Преобразовать список RouteDto в формат для вывода пользователю.
     */
    String toPrettyString(List<RouteDto> routes);

    /**
     * Преобразовать список RouteDto в формат для поиска билетов.
     */
    List<String> toFindTicketsList(List<RouteDto> routes);
}
