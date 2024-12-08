package net.osandman.rzdmonitoring.mapping;

import net.osandman.rzdmonitoring.client.dto.route.RootRoute;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static org.springframework.util.StringUtils.hasText;

@Component
public class RouteMapper {

    /**
     * Преобразование сущности с маршрутами поездов в строку для удобного чтения в телеграм.
     */
    public String getPrettyString(RootRoute rootRoute) {
        List<String> routes = new ArrayList<>();
        rootRoute.tp.stream().collect(Collectors.toMap(tp -> tp.from, tp -> tp))
            .values().forEach(el -> el.list
                .forEach(route -> routes.add(
                        String.format("\uD83D\uDE9D %s%s, из %s - %s в %s, прибытие в %s - %s в %s", // ➤ 🚝
                            route.number, hasText(route.brand) ? "(" + route.brand + ")" : "",
                            route.station0,
                            route.localDate0 != null ? route.localDate0 : route.date0,
                            route.localTime0 != null ? route.localTime0 : route.time0,
                            route.station1,
                            route.localDate1 != null ? route.localDate1 : route.date1,
                            route.localTime1 != null ? route.localTime1 : route.time1)
                    )
                ));
        return String.join(System.lineSeparator(), routes);
    }
}
