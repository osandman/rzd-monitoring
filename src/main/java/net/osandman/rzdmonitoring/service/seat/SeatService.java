package net.osandman.rzdmonitoring.service.seat;

import net.osandman.rzdmonitoring.dto.TicketsResult;
import net.osandman.rzdmonitoring.scheduler.TicketsTask;

import java.util.List;

public interface SeatService {

    TicketsResult monitoringProcess(TicketsTask ticketsTask, List<SeatFilter> seatFilters);
}
