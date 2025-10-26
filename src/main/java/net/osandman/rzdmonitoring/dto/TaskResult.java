package net.osandman.rzdmonitoring.dto;

import net.osandman.rzdmonitoring.scheduler.TicketsTask;

public record TaskResult(boolean success, String msg, TicketsTask ticketsTask) {
}
