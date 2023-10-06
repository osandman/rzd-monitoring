package net.osandman.rzdmonitoring.bot.command;

import net.osandman.rzdmonitoring.entity.Station;
import net.osandman.rzdmonitoring.service.RzdMonitoringService;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.objects.Update;

@Component
public class FindRoutesCommand extends TelegramCommand {
    private final RzdMonitoringService monitoringService;

    public FindRoutesCommand(RzdMonitoringService monitoringService) {
        this.monitoringService = monitoringService;
        command = CommandEnum.ROUTES.name;
    }

    @Override
    public void handleCommand(TelegramLongPollingBot sender, Update update) {
        super.handleCommand(sender, update);
        String message = monitoringService.getRoutes(Station.MOSCOW_ALL.name(), Station.PERM_ALL.name(), null);
        super.sendMessage(sender, message);
    }
}
