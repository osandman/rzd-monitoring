package net.osandman.rzdmonitoring.bot.command;

import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;

@Component
public class UnknownCommand extends AbstractTelegramCommand implements ITelegramCommand {

    @Override
    public void handleCommand(Update update) {
        long chatId = update.getMessage().getChatId();
        sendMessage(chatId, getCommand().getDesc());
    }

    @Override
    public Command getCommand() {
        return Command.UNKNOWN;
    }

    @Override
    public boolean canToShow() {
        return false;
    }
}
