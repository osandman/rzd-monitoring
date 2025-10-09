package net.osandman.rzdmonitoring.service.seat;

import net.osandman.rzdmonitoring.dto.train.TicketsResult;
import net.osandman.rzdmonitoring.scheduler.TicketsTask;

import java.util.Set;

public interface TicketService {

    TicketsResult monitoringProcess(TicketsTask ticketsTask, Set<SeatFilter> seatFilters);
}
