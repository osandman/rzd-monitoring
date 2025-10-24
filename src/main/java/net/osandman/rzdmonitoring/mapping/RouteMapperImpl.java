package net.osandman.rzdmonitoring.mapping;

import net.osandman.rzdmonitoring.client.dto.v2.route.CarGroup;
import net.osandman.rzdmonitoring.client.dto.v2.route.RootRouteDto;
import net.osandman.rzdmonitoring.client.dto.v2.route.Train;
import net.osandman.rzdmonitoring.dto.route.CarriageDto;
import net.osandman.rzdmonitoring.dto.route.RouteDto;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import static org.springframework.util.StringUtils.hasText;

@Component
public class RouteMapperImpl implements RouteMapper {

    public static final String TRAIN_ICON = "\uD83D\uDE9D"; // 🚝

    @Override
    public List<RouteDto> toRoutes(RootRouteDto response) {
        List<RouteDto> routes = new ArrayList<>();

        if (response.getTrains() == null || response.getTrains().isEmpty()) {
            return routes;
        }

        for (Train train : response.getTrains()) {
            RouteDto route = new RouteDto();
            route.setTrainNumber(train.getTrainNumber());
            route.setDisplayTrainNumber(train.getDisplayTrainNumber());
            route.setTrainName(train.getTrainName());
            route.setFromStation(train.getOriginName());
            route.setToStation(train.getDestinationName());
            route.setFromStationCode(train.getOriginStationCode());
            route.setToStationCode(train.getDestinationStationCode());
            DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
            route.setDepartureDateTime(LocalDateTime.parse(train.getLocalDepartureDateTime(), formatter));
            route.setArrivalDateTime(LocalDateTime.parse(train.getLocalArrivalDateTime(), formatter));
            route.setIsSuburban(train.getIsSuburban());
            route.setCarrier(
                (train.getCarriers() != null && !train.getCarriers().isEmpty()) ? train.getCarriers().get(0) : null
            );
            // Обработка вагонов
            List<CarriageDto> carriages = new ArrayList<>();
            if (train.getCarGroups() != null && !train.getCarGroups().isEmpty()) {
                for (CarGroup carGroup : train.getCarGroups()) {
                    CarriageDto carriage = mapCarGroupToCarriage(carGroup);
                    carriages.add(carriage);
                }
            }
            route.setCarriages(carriages);
            routes.add(route);
        }
        return routes;
    }

    private static CarriageDto mapCarGroupToCarriage(CarGroup carGroup) {
        CarriageDto carriage = new CarriageDto();
        carriage.setType(carGroup.getCarType());
        carriage.setTypeName(carGroup.getCarTypeName());
        carriage.setServiceClass(carGroup.getServiceClasses() != null && !carGroup.getServiceClasses().isEmpty() ?
            carGroup.getServiceClasses().get(0) : null);
        carriage.setLowerSeats(carGroup.getLowerPlaceQuantity() != null ? carGroup.getLowerPlaceQuantity() : 0);
        carriage.setUpperSeats(carGroup.getUpperPlaceQuantity() != null ? carGroup.getUpperPlaceQuantity() : 0);
        carriage.setLowerSideSeats(carGroup.getLowerSidePlaceQuantity() != null ? carGroup.getLowerSidePlaceQuantity() : 0);
        carriage.setUpperSideSeats(carGroup.getUpperSidePlaceQuantity() != null ? carGroup.getUpperSidePlaceQuantity() : 0);
        carriage.setTotalSeats(carGroup.getTotalPlaceQuantity() != null ? carGroup.getTotalPlaceQuantity() : 0);
        return carriage;
    }

    @Override
    public String toPrettyString(List<RouteDto> routes) {
        if (routes == null || routes.isEmpty()) {
            return "🚫 Маршруты не найдены";
        }
        List<String> result = getFullInfo(TRAIN_ICON, routes);
        return String.join(System.lineSeparator(), result);
    }

    @Override
    @NonNull
    public List<String> toFindTicketsList(List<RouteDto> routes) {
        if (routes == null || routes.isEmpty()) {
            return List.of();
        }
        return getSmallInfo(routes);
    }

    private static List<String> getFullInfo(String prefix, List<RouteDto> routes) {
        List<String> result = new ArrayList<>();
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
        String departureDate = routes.get(0).getDepartureDateTime().format(dateFormatter);
        for (RouteDto route : routes) {
            String departureTime = route.getDepartureDateTime() != null ?
                route.getDepartureDateTime().format(timeFormatter) : "N/A";
            String arrivalDate = route.getArrivalDateTime() != null ?
                route.getArrivalDateTime().format(dateFormatter) : "N/A";
            String arrivalTime = route.getArrivalDateTime() != null ?
                route.getArrivalDateTime().format(timeFormatter) : "N/A";
            String routeInfo = prefix + " %s%s%s%s отправление в %s прибытие %s в %s, %s".formatted(
                route.getTrainNumber(),
                hasText(route.getDisplayTrainNumber()) && !route.getDisplayTrainNumber().equals(route.getTrainNumber())
                    ? " (" + route.getDisplayTrainNumber() + ")" : "",
                hasText(route.getTrainName()) ? " \"" + route.getTrainName() + "\" " : "",
                route.getIsSuburban() ? " (пригород.) " : "",
                departureTime,
                arrivalDate.equals(departureDate) ? "" : arrivalDate,
                arrivalTime,
                route.getIsSuburban() ? "" : "свободных мест " + route.getCarriages().stream()
                    .mapToInt(CarriageDto::getTotalSeats)
                    .sum()
            );
            result.add(routeInfo);
        }
        return result;
    }

    private static List<String> getSmallInfo(List<RouteDto> routes) {
        List<String> result = new ArrayList<>();
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd.MM.yy");
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
        String departureDate = routes.get(0).getDepartureDateTime().format(dateFormatter);
        for (RouteDto route : routes) {
            String departureTime = route.getDepartureDateTime() != null ?
                route.getDepartureDateTime().format(timeFormatter) : "N/A";
            String arrivalDate = route.getArrivalDateTime() != null ?
                route.getArrivalDateTime().format(dateFormatter) : "N/A";
            String arrivalTime = route.getArrivalDateTime() != null ?
                route.getArrivalDateTime().format(timeFormatter) : "N/A";
            String routeInfo = "%s%s%s в %s → %s в %s %s ".formatted(
                route.getTrainNumber(),
                hasText(route.getDisplayTrainNumber()) && !route.getDisplayTrainNumber().equals(route.getTrainNumber())
                    ? " (" + route.getDisplayTrainNumber() + ")" : "",
                route.getIsSuburban() ? " (пригород.) " : "",
                departureTime,
                arrivalDate.equals(departureDate) ? "" : arrivalDate,
                arrivalTime,
                route.getIsSuburban() ? "" : "мест " + route.getCarriages().stream()
                    .mapToInt(CarriageDto::getTotalSeats)
                    .sum()
            );
            result.add(routeInfo);
        }
        return result;
    }
}
