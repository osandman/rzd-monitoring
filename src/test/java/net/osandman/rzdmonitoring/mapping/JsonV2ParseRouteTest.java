package net.osandman.rzdmonitoring.mapping;

import lombok.SneakyThrows;
import net.osandman.rzdmonitoring.client.dto.v2.route.RootRouteDto;
import net.osandman.rzdmonitoring.dto.route.RouteDto;
import net.osandman.rzdmonitoring.util.JsonParser;
import org.junit.jupiter.api.Test;

import java.util.List;

import static java.util.Objects.requireNonNull;
import static org.assertj.core.api.Assertions.assertThat;

class JsonV2ParseRouteTest {

    private final RouteMapperImpl routeMapper = new RouteMapperImpl();

    @Test
    @SneakyThrows
    void ekbToOmskMappingTest() {
        RootRouteDto rootTrain = JsonParser.parse(
            requireNonNull(this.getClass().getClassLoader().getResource("json/routes/екб-омск.json"))
                .openStream(),
            RootRouteDto.class
        );
        List<RouteDto> routes = routeMapper.toRoutes(rootTrain);
        assertThat(routes).isNotEmpty();
    }

    @Test
    @SneakyThrows
    void permToLysvaMappingTest() {
        RootRouteDto rootTrain = JsonParser.parse(
            requireNonNull(this.getClass().getClassLoader().getResource("json/routes/пермь-лысьва.json"))
                .openStream(),
            RootRouteDto.class
        );
        List<RouteDto> routes = routeMapper.toRoutes(rootTrain);
        assertThat(routes).isNotEmpty();
        assertThat(routes).hasSize(1);
        assertThat(routes.get(0).getCarriages()).hasSize(0);
    }

    @Test
    @SneakyThrows
    void nabChelnToMoscowMappingTest() {
        RootRouteDto rootTrain = JsonParser.parse(
            requireNonNull(this.getClass().getClassLoader().getResource("json/routes/набчелны-москва.json"))
                .openStream(),
            RootRouteDto.class
        );
        List<RouteDto> routes = routeMapper.toRoutes(rootTrain);
        assertThat(routes).isNotEmpty();
    }
}