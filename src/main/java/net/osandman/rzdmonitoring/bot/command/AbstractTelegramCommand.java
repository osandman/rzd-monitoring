package net.osandman.rzdmonitoring.bot.command;

import lombok.RequiredArgsConstructor;
import net.osandman.rzdmonitoring.dto.StationDto;
import net.osandman.rzdmonitoring.entity.UserState;
import net.osandman.rzdmonitoring.repository.UserStateRepository;
import net.osandman.rzdmonitoring.service.StationService;
import net.osandman.rzdmonitoring.util.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
public abstract class AbstractTelegramCommand {

    @Autowired
    protected StationService stationService;

    @Autowired
    protected UserStateRepository userStateRepository;

    @Autowired
    @Lazy // для избежания циклической зависимости реализаций команд с List<ITelegramCommand> commands
    protected TelegramLongPollingBot sender;
    protected final Logger log = LoggerFactory.getLogger(getClass().getSimpleName());
    public static final String DATE_FORMAT_PATTERN = "dd.MM.yyyy";

    protected void sendMessage(long chatId, String message) {
        int maxLength = 4096;
        List<String> messageBlocks = new ArrayList<>();
        while (message.length() > maxLength) {
            String block = message.substring(0, maxLength);
            messageBlocks.add(block);
            message = message.substring(maxLength);
        }
        if (!message.isEmpty()) {
            messageBlocks.add(message);
        }
        for (String block : messageBlocks) {
            SendMessage sendMessage = new SendMessage(String.valueOf(chatId), block);
            executeMessage(sendMessage);
            Utils.sleep(1000);
        }
    }

    protected void executeMessage(SendMessage sendMessage) {
        try {
            sender.execute(sendMessage);
            log.info("Сообщение '{}' отправлено пользователю, chatId={}",
                sendMessage.getText(), sendMessage.getChatId());
        } catch (TelegramApiException e) {
            log.error("Ошибка при отправке сообщения", e);
        } catch (Exception e) {
            log.error("Произошла ошибка", e);
        }
    }

    protected <T> void sendButtons(long chatId, String message, List<T> toButtons) {
        ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();
        keyboardMarkup.setSelective(true);
        keyboardMarkup.setResizeKeyboard(true);
        keyboardMarkup.setOneTimeKeyboard(true);

        List<KeyboardRow> keyboard = new ArrayList<>();
        KeyboardRow currentRow = new KeyboardRow();
        List<KeyboardButton> buttons = new ArrayList<>();

        for (T button : toButtons) {
            buttons.add(new KeyboardButton(button.toString()));
            if (buttons.size() >= 4) {
                currentRow.addAll(buttons);
                keyboard.add(currentRow);
                buttons.clear();
                currentRow = new KeyboardRow();
            }
        }
        if (!buttons.isEmpty()) {
            currentRow.addAll(buttons);
            keyboard.add(currentRow);
        }
        keyboardMarkup.setKeyboard(keyboard);
        SendMessage sendMessage = new SendMessage(String.valueOf(chatId), message);
        sendMessage.setReplyMarkup(keyboardMarkup);
        executeMessage(sendMessage);
    }


    protected void findStations(String messageText, UserState.CommandState commandState, int step, long chatId) {
        List<StationDto> fromStationDtos = stationService.findStations(messageText);
        if (fromStationDtos.isEmpty()) {
            commandState.setStep(step);
            sendMessage(chatId, "Станция '%s' не найдена, введите заново".formatted(messageText));
            return;
        }
        sendButtons(chatId, "Выберите станцию:", fromStationDtos);
        commandState.incrementStep();
    }

    protected StationDto getStationDto(String messageText, List<StationDto> toStationDtos) {
        return toStationDtos == null ? null :
            toStationDtos.stream()
                .filter(stationDto -> stationDto.name().equalsIgnoreCase(messageText))
                .findAny().orElse(null);
    }

    protected LocalDate parseDate(String dateStr) {
        try {
            return LocalDate.parse(dateStr,
                DateTimeFormatter.ofPattern(AbstractTelegramCommand.DATE_FORMAT_PATTERN));
        } catch (DateTimeParseException e) {
            return null;
        }
    }
}