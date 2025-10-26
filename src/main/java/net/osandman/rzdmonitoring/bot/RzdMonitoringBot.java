package net.osandman.rzdmonitoring.bot;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.osandman.rzdmonitoring.bot.command.ITelegramCommand;
import net.osandman.rzdmonitoring.entity.UserState;
import net.osandman.rzdmonitoring.jpa.service.UserService;
import net.osandman.rzdmonitoring.repository.UserStateRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.objects.MaybeInaccessibleMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.List;

import static net.osandman.rzdmonitoring.bot.command.Command.UNKNOWN;

@Component
@Slf4j
@Getter
public class RzdMonitoringBot extends TelegramLongPollingBot {

    @Value("${bot.username}")
    private String botUserName;

    private final List<ITelegramCommand> telegramCommands;
    private final UserStateRepository userStateRepository;
    private final UserService userService;

    @Autowired
    public RzdMonitoringBot(
        @Value("${bot.token}") String botToken,
        @Qualifier("sortedTelegramCommands")
        List<ITelegramCommand> telegramCommands, UserStateRepository userStateRepository, UserService userService
    ) {
        super(botToken);
        this.telegramCommands = telegramCommands;
        this.userStateRepository = userStateRepository;
        this.userService = userService;
    }

    @Override
    public void onUpdateReceived(@NonNull Update update) {

        userService.createOrUpdate(update);

        if (update.hasMessage()) {
            handleMessage(update);
        } else if (update.hasCallbackQuery()) {
            handleCallback(update);
        }
    }

    private void handleMessage(@NonNull Update update) {
        Message message = update.getMessage();
        long chatId = message.getChatId();
        log.debug("Поступил update для chatId={}, текст='{}'", chatId, message.getText());
        if (!update.hasMessage()) {
            log.info("Update не содержит сообщений");
            return;
        }
        if (message.isCommand()) {
            userStateRepository.getOrCreate(chatId).deleteAllCommands();
            String messageText = message.getText();
            for (ITelegramCommand telegramCommand : telegramCommands) {
                String commandStr = telegramCommand.getCommand().getCommandStr();
                if (messageText.equalsIgnoreCase(commandStr)) {
                    telegramCommand.handleCommand(update);
                    return;
                }
            }
            telegramCommands.stream()
                .filter(command -> UNKNOWN.equals(command.getCommand()))
                .findAny().ifPresent(command -> command.handleCommand(update));
        } else if (message.hasText()) {
            UserState userState = userStateRepository.get(chatId);
            for (ITelegramCommand telegramCommand : telegramCommands) {
                // TODO подумать нужно ли хранить набор команд, если по факту только одна активная, остальные обнуляются
                if (userState != null && userState.getUserStates().containsKey(telegramCommand.getCommand())) {
                    telegramCommand.handleCommand(update);
                    return;
                }
            }
        }
    }

    private void handleCallback(@NonNull Update update) {
        MaybeInaccessibleMessage message = update.getCallbackQuery().getMessage();
        long chatId = message.getChatId();
        UserState userState = userStateRepository.get(chatId);
        for (ITelegramCommand telegramCommand : telegramCommands) {
            // TODO подумать нужно ли хранить набор команд, если по факту только одна активная, остальные обнуляются
            if (userState != null && userState.getUserStates().containsKey(telegramCommand.getCommand())) {
                telegramCommand.handleCommand(update);
                return;
            }
        }
    }

    @Override
    public String getBotUsername() {
        return botUserName;
    }
}
