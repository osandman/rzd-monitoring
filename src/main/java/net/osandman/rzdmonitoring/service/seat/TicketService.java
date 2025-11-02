package net.osandman.rzdmonitoring.service.seat;

import net.osandman.rzdmonitoring.client.dto.v2.train.RootTrainDto;
import net.osandman.rzdmonitoring.dto.train.TicketsResult;
import net.osandman.rzdmonitoring.scheduler.TicketsTask;

import java.util.Set;

public interface TicketService {

    TicketsResult monitoringProcess(TicketsTask ticketsTask, Set<SeatFilter> seatFilters);

    RootTrainDto getRootTrainDto(String fromCode, String toCode, String departureDate, String trainNumber);
}
