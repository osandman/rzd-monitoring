package net.osandman.rzdmonitoring.bot.command;

import net.osandman.rzdmonitoring.service.RouteService;
import net.osandman.rzdmonitoring.service.StationService;
import net.osandman.rzdmonitoring.service.TicketService;
import org.telegram.telegrambots.meta.api.objects.Update;

public class UnknownCommand extends TelegramCommand {

    public UnknownCommand(TicketService ticketService, RouteService routeService,
                          StationService stationService) {
        super(ticketService, routeService, stationService, null);
    }

    @Override
    public void handleCommand(Update update) {
        long chatId = update.getMessage().getChatId();
        sendMessage(chatId, "Команда не найдена");
    }
}
