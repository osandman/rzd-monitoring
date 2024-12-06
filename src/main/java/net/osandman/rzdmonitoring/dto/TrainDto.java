package net.osandman.rzdmonitoring.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public final class TrainDto {
    private String trainNumber;
    private String fromStation;
    private String toStation;
    private LocalDateTime dateTimeFrom;
    private LocalDateTime dateTimeTo;
    private List<SeatDto> seats;
    private String error;
}
