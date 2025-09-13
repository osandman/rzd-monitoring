package net.osandman.rzdmonitoring.util;

import net.osandman.rzdmonitoring.client.dto.v2.train.RootDto;
import net.osandman.rzdmonitoring.dto.SeatDto;
import net.osandman.rzdmonitoring.dto.TrainDto;
import net.osandman.rzdmonitoring.mapping.TrainMapperImpl;
import net.osandman.rzdmonitoring.service.seat.SeatFilter;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;

import static java.util.Objects.requireNonNull;
import static org.assertj.core.api.Assertions.assertThat;

class TrainJsonV2ParseTest {

    @Test
    void parseTrain068() throws IOException {
        RootDto rootTrain = JsonParser.parse(
            requireNonNull(this.getClass().getClassLoader().getResource("json/068Ы_Москва-Пермь.json"))
                .openStream(),
            RootDto.class
        );
        assertThat(rootTrain).isNotNull();

        TrainMapperImpl ticketMapper = new TrainMapperImpl();
        TrainDto trainDto = ticketMapper.map(rootTrain);
        assertThat(trainDto).isNotNull();
        assertThat(trainDto.getSeats()).hasSize(11);

        List<SeatFilter> seatFilters = List.of(SeatFilter.DOWN_SEATS, SeatFilter.COMPARTMENT);
        List<SeatDto> filteredSeats = trainDto.getSeats().stream()
            .filter(seatDto ->
                seatFilters.stream().allMatch(seatFilter -> seatFilter.getPredicate().test(seatDto))
            )
            .toList();
        assertThat(filteredSeats).hasSize(2);
    }

    @Test
    void parseTrain114() throws IOException {
        RootDto rootTrain = JsonParser.parse(
            requireNonNull(this.getClass().getClassLoader().getResource("json/114Э_Краснодар-Москва.json"))
                .openStream(),
            RootDto.class
        );
        assertThat(rootTrain).isNotNull();

        TrainMapperImpl ticketMapper = new TrainMapperImpl();
        TrainDto trainDto = ticketMapper.map(rootTrain);
        assertThat(trainDto).isNotNull();
        assertThat(trainDto.getSeats()).hasSize(35);

        List<SeatFilter> seatFilters = List.of(SeatFilter.DOWN_SEATS, SeatFilter.COMPARTMENT, SeatFilter.NOT_INVALID);
        List<SeatDto> filteredSeats = trainDto.getSeats().stream()
            .filter(seatDto ->
                seatFilters.stream().allMatch(seatFilter -> seatFilter.getPredicate().test(seatDto))
            )
            .toList();
        assertThat(filteredSeats).hasSize(2);

        List<SeatFilter> seatFiltersInvalid = List.of(SeatFilter.FOR_INVALID);
        List<SeatDto> filteredSeatsInvalid = trainDto.getSeats().stream()
            .filter(seatDto ->
                seatFiltersInvalid.stream().allMatch(seatFilter -> seatFilter.getPredicate().test(seatDto))
            )
            .toList();
        assertThat(filteredSeatsInvalid).hasSize(1);
    }
}