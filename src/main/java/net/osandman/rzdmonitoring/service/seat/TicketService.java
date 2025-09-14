package net.osandman.rzdmonitoring.service.seat;

import net.osandman.rzdmonitoring.dto.train.TicketsResult;
import net.osandman.rzdmonitoring.scheduler.TicketsTask;

import java.util.List;

public interface TicketService {

    TicketsResult monitoringProcess(TicketsTask ticketsTask, List<SeatFilter> seatFilters);
}
