package net.osandman.rzdmonitoring.bot.command;

import net.osandman.rzdmonitoring.service.RouteService;
import net.osandman.rzdmonitoring.service.StationService;
import net.osandman.rzdmonitoring.service.TicketService;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.List;

@Component
public class StartCommand extends TelegramCommand {
    private final List<TelegramCommand> commands;
    private final static String MESSAGE = """
        Привет!
        Добро пожаловать, %s!
        Это бот для получения информации о маршрутах и билетах РЖД.
        Доступны команды:
        %s
        """;

    public StartCommand(TicketService ticketService, RouteService routeService, StationService stationService,
                        List<TelegramCommand> commands) {
        super(ticketService, routeService, stationService, CommandEnum.START.name);
        this.commands = commands;
    }

    @Override
    public void handleCommand(Update update) {
        long chatId = update.getMessage().getChatId();
        Chat chat = update.getMessage().getChat();
        String fullName = chat.getFirstName() + " " + chat.getLastName();
        String allCommands = String.join(System.lineSeparator(),
            commands.stream().map(TelegramCommand::getCommandName).toList());
        String message = MESSAGE.formatted(fullName, allCommands);
        sendMessage(chatId, message);
        sendButtons(chatId, "Выберите опцию:", commands);
    }
}