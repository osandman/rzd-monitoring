package net.osandman.rzdmonitoring.api;

import lombok.RequiredArgsConstructor;
import net.osandman.rzdmonitoring.dto.TicketsResult;
import net.osandman.rzdmonitoring.dto.station.StationDto;
import net.osandman.rzdmonitoring.scheduler.TicketsTask;
import net.osandman.rzdmonitoring.service.TicketService;
import net.osandman.rzdmonitoring.service.station.StationService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/v1/rzd")
@RequiredArgsConstructor
public class RzdController {

    private final StationService stationService;
    private final TicketService ticketService;

    @GetMapping("/station")
    private List<StationDto> findStation(@RequestParam String name) {
        return stationService.findStations(name);
    }

    @GetMapping("/tickets")
    private TicketsResult findTickets(
        @RequestBody TicketsTask ticketsTask
    ) {
        return ticketService.process(ticketsTask);
    }
}
