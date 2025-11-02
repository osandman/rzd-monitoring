package net.osandman.rzdmonitoring.mapping;

import net.osandman.rzdmonitoring.client.dto.v2.train.CarDto;
import net.osandman.rzdmonitoring.client.dto.v2.train.RootTrainDto;
import net.osandman.rzdmonitoring.client.dto.v2.train.TrainInfoDto;
import net.osandman.rzdmonitoring.dto.train.SeatDto;
import net.osandman.rzdmonitoring.dto.train.TrainDto;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class TrainMapperImpl implements TrainMapper {

    @Override
    @NonNull
    public TrainDto map(RootTrainDto json) {
        // Если нет информации о поезде, возвращаем пустой результат
        TrainInfoDto trainInfo = json.getTrainInfo();
        if (trainInfo == null) {
            return TrainDto.builder()
                .error("Не найдена информация о поезде")
                .build();
        }
        TrainDto trainDto;
        try {
            // Преобразуем даты из строк в LocalDateTime
            DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
            LocalDateTime departure = LocalDateTime.parse(trainInfo.getDepartureDateTime(), formatter);
            LocalDateTime arrival = LocalDateTime.parse(trainInfo.getArrivalDateTime(), formatter);
            trainDto = TrainDto.builder()
                .trainNumber(trainInfo.getTrainNumber())
                .fromStation(trainInfo.getOriginName())
                .toStation(trainInfo.getDestinationName())
                .dateTimeFrom(departure)
                .dateTimeTo(arrival)
                .seats(mapCarsToSeats(json.getCars()))
                .build();
        } catch (Exception e) {
            return TrainDto.builder()
                .trainNumber(trainInfo.getTrainNumber())
                .error(e.getMessage())
                .build();
        }
        return trainDto;
    }

    private List<SeatDto> mapCarsToSeats(List<CarDto> cars) {
        if (CollectionUtils.isEmpty(cars)) {
            return List.of();
        }
        return cars.stream()
            .filter(carDto -> // фильтруем по признаку если нет карт оплаты - продажи еще не начались,
                // либо через признак IsThreeHoursReservationAvailable = false ?
                !CollectionUtils.isEmpty(carDto.getRzhdCardTypes()))
            .map(this::convertCarToSeat)
            .collect(Collectors.toList());
    }

    private SeatDto convertCarToSeat(CarDto car) {
        return new SeatDto(
            car.getCarNumber(),
            car.getCarTypeName(),
            car.getCarPlaceNameRu(),
            car.getFreePlaces(),
            formatPrice(car),
            car.getPlaceQuantity()
        );
    }

    private String formatPrice(CarDto car) {
        // Форматируем цену: если min и max совпадают, показываем одно значение, иначе диапазон
        if (car.getMinPrice().equals(car.getMaxPrice())) {
            return String.format("%.2f", car.getMinPrice());
        }
        return String.format("%.2f-%.2f", car.getMinPrice(), car.getMaxPrice());
    }
}
