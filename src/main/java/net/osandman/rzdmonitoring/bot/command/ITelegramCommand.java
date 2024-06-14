package net.osandman.rzdmonitoring.bot.command;

import org.telegram.telegrambots.meta.api.objects.Update;

public interface ITelegramCommand {

    void handleCommand(Update update);

    String getCommand();

    boolean canToShow();
}
