package net.osandman.rzdmonitoring.service.route;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.osandman.rzdmonitoring.client.RestTemplateConnector;
import net.osandman.rzdmonitoring.client.dto.v2.route.RootRouteDto;
import net.osandman.rzdmonitoring.dto.route.RouteDto;
import net.osandman.rzdmonitoring.dto.route.RoutesResult;
import net.osandman.rzdmonitoring.mapping.RouteMapper;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class RouteServiceImpl implements RouteService {

    private final RestTemplateConnector restTemplateConnector;
    private final RouteMapper routeMapper;

    @Override
    public RoutesResult findRoutes(String fromStationCode, String toStationCode, String departureDate) {

        String baseUrl = "https://ticket.rzd.ru";
        String endpoint = "/api/v1/railway-service/prices/train-pricing";

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("service_provider", "B2B_RZD");
        params.add("getByLocalTime", "true");
        params.add("carGrouping", "DontGroup");
        params.add("origin", fromStationCode);
        params.add("destination", toStationCode);
        params.add("departureDate", departureDate);
        params.add("specialPlacesDemand", "StandardPlacesAndForDisabledPersons");
        params.add("carIssuingType", "Passenger");
        params.add("getTrainsFromSchedule", "true");
        params.add("adultPassengersQuantity", "1");
        params.add("childrenPassengersQuantity", "0");
        params.add("hasPlacesForLargeFamily", "false");

        RootRouteDto rootRouteDto = restTemplateConnector.callGetRequest(baseUrl, endpoint, params, RootRouteDto.class);
        List<RouteDto> routes = routeMapper.toRoutes(rootRouteDto);

        return RoutesResult.builder()
            .routesCount(routes.size())
            .routes(routes)
            .build();
    }

    @Override
    public String getRoutesAsString(String fromStationCode, String toStationCode, String departureDate) {
        String prettyString;
        try {
            RoutesResult routesResult = findRoutes(fromStationCode, toStationCode, departureDate);
            prettyString = routeMapper.toPrettyString(routesResult.routes());
        } catch (Exception e) {
            log.error("Произошла ошибка при обработке маршрутов", e);
            prettyString = "Произошла ошибка, обратитесь к разработчику";
        }
        return prettyString;
    }
}
