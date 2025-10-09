package net.osandman.rzdmonitoring.bot.command;

import io.github.dostonhamrakulov.InlineCalendarBuilder;
import io.github.dostonhamrakulov.InlineCalendarCommandUtil;
import io.github.dostonhamrakulov.LanguageEnum;
import lombok.RequiredArgsConstructor;
import net.osandman.rzdmonitoring.bot.RzdMonitoringBot;
import net.osandman.rzdmonitoring.dto.station.StationDto;
import net.osandman.rzdmonitoring.entity.MultiSelectType;
import net.osandman.rzdmonitoring.entity.UserState;
import net.osandman.rzdmonitoring.repository.UserStateRepository;
import net.osandman.rzdmonitoring.service.seat.SeatFilter;
import net.osandman.rzdmonitoring.service.station.StationService;
import net.osandman.rzdmonitoring.util.Utils;
import net.osandman.rzdmonitoring.validate.CheckDateResult;
import net.osandman.rzdmonitoring.validate.Validator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.lang.Nullable;
import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.InaccessibleMessage;
import org.telegram.telegrambots.meta.api.objects.MaybeInaccessibleMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public abstract class AbstractTelegramCommand implements ITelegramCommand {

    @Autowired
    protected StationService stationService;

    @Autowired
    protected UserStateRepository userStateRepository;

    @Autowired
    protected Validator validator;

    @Autowired
    @Lazy // для избежания циклической зависимости реализаций команд с Set<ITelegramCommand> commands
    protected RzdMonitoringBot sender;

    protected final Logger log = LoggerFactory.getLogger(getClass().getSimpleName());
    public static final String DATE_FORMAT_PATTERN = "dd.MM.yyyy";

    protected CommandContext buildCommandContext(Update update, Command command) {
        MaybeInaccessibleMessage message = getMessage(update);
        long chatId = message.getChatId();
        String messageText = null;
        String userName = null;
        if (message instanceof Message mes) {
            messageText = mes.getText();
            userName = mes.getChat().getUserName();
        } else if (message instanceof InaccessibleMessage mes) {
            userName = mes.getChat().getUserName();
        }

        log.info("Сообщение '{}' получено от пользователя {}, chatId={}", messageText, userName, chatId);

        UserState userState = userStateRepository.getOrCreate(chatId);
        // устанавливает команду если ее не было
        UserState.CommandState commandState = userState.getOrCreateCommandState(command);
        return new CommandContext(chatId, messageText, commandState);
    }

    private MaybeInaccessibleMessage getMessage(Update update) {
        if (update.getMessage() != null) {
            return update.getMessage();
        }
        return update.getCallbackQuery().getMessage();
    }

    protected record CommandContext(long chatId, String messageText, UserState.CommandState state) {
    }

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

    protected <T extends Serializable, Method extends BotApiMethod<T>> T executeMessage(Method method) {
        T result = null;
        try {
            result = sender.execute(method);
            if (method instanceof SendMessage sendMessage) {
                log.info("Сообщение '{}' отправлено пользователю, chatId={}",
                    sendMessage.getText(), sendMessage.getChatId());
            }
        } catch (TelegramApiException e) {
            log.error("Ошибка при отправке сообщения", e);
        } catch (Exception e) {
            log.error("Произошла непредвиденная ошибка", e);
        }
        return result;
    }

    protected <T> void sendButtons(long chatId, String message, List<T> toButtons) {
        sendButtons(chatId, message, toButtons, 3);
    }

    protected <T> void sendButtons(long chatId, String message, List<T> toButtons, int totalColumns) {
        ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();
        keyboardMarkup.setSelective(true);
        keyboardMarkup.setResizeKeyboard(true);
        keyboardMarkup.setOneTimeKeyboard(true);

        List<KeyboardRow> keyboard = new ArrayList<>();
        KeyboardRow currentRow = new KeyboardRow();
        List<KeyboardButton> buttons = new ArrayList<>();

        for (T button : toButtons) {
            buttons.add(new KeyboardButton(button.toString()));
            if (buttons.size() >= totalColumns) {
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

    protected void sendCalendar(long chatId, String message, Update update) {
        InlineCalendarBuilder inlineCalendarBuilder = new InlineCalendarBuilder(LanguageEnum.RU);
        inlineCalendarBuilder.setShowFullMonthName(false);
        SendMessage sendMessage = new SendMessage(String.valueOf(chatId), message);
        sendMessage.setReplyMarkup(inlineCalendarBuilder.build(update));
        executeMessage(sendMessage);
    }

    protected LocalDate handleDate(Update update, CommandContext command) {
        LocalDate localDate;
        CheckDateResult checkDateResult;
        if (InlineCalendarCommandUtil.isInlineCalendarClicked(update)) {
            // if ignorable buttons are clicked like empty cells, cells for week days, etc.
            if (InlineCalendarCommandUtil.isCalendarIgnoreButtonClicked(update)) {
                return null;
            }
            // return to the next or previous months
            if (InlineCalendarCommandUtil.isCalendarNavigationButtonClicked(update)) {
                sendCalendar(command.chatId(), "Введите дату отправления:", update);
                return null;
            }
            localDate = InlineCalendarCommandUtil.extractDate(update);
            checkDateResult = validator.dateValidate(localDate);
        } else {
            String dateStr = command.messageText();
            checkDateResult = validator.dateValidate(dateStr);
        }

        if (!checkDateResult.valid()) {
            sendMessage(command.chatId(), checkDateResult.message());
            return null;
        }
        return checkDateResult.localDate();
    }

    protected void findAndShowStationsAndIncrementStep(
        String messageText, UserState.CommandState commandState, long chatId
    ) {
        List<StationDto> stationDtos = stationService.findStations(messageText);
        if (stationDtos.isEmpty()) {
            sendMessage(chatId, "Станция '%s' не найдена, введите заново".formatted(messageText));
            return;
        }
        sendButtons(chatId, "Выберите станцию или введите текст для нового поиска:", stationDtos);
        commandState.incrementStep();
    }

    protected boolean checkStationAndSetStates(CommandContext command, ParamType stationCode, ParamType stationName) {
        StationDto stationDto = getFoundStationDto(
            command.messageText(), stationService.findStations(command.messageText())
        );
        if (stationDto == null) {
            findAndShowStationsAndIncrementStep(command.messageText(), command.state(), command.chatId());
            command.state().decrementStep();
            return false;
        }
        command.state().addKey(stationCode, stationDto.code());
        command.state().addKey(stationName, stationDto.name());
        return true;
    }

    protected StationDto getFoundStationDto(String searchName, List<StationDto> toStationDtos) {
        return toStationDtos == null ? null :
            toStationDtos.stream()
                .filter(stationDto -> stationDto.name().equalsIgnoreCase(searchName))
                .findAny().orElse(null);
    }

    protected void sendMultiSelectButtons(
        long chatId, MultiSelectType type, String initialMessage, Set<?> options
    ) {
        UserState userState = userStateRepository.get(chatId);
        UserState.CommandState commandState = userState.getOrCreateCommandState(getCommand());
        UserState.MultiSelect multiSelect = commandState.createMultiSelectParam(type, initialMessage);

        InlineKeyboardMarkup inlineKeyboardMarkup = createMultiSelectMarkup(
            options, multiSelect.getSelectedOptions(), type.getColumnCount()
        );

        SendMessage sendMessage = new SendMessage(String.valueOf(chatId), multiSelect.getSelectedText());
        sendMessage.setReplyMarkup(inlineKeyboardMarkup);

        Message sentMessage = executeMessage(sendMessage);
        if (sentMessage != null) {
            multiSelect.setMessageId(sentMessage.getMessageId());
        }
    }

    private InlineKeyboardMarkup createMultiSelectMarkup(
        Set<?> options, Set<String> selectedOptions, int columnsCount
    ) {
        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();
        List<InlineKeyboardButton> currentRow = new ArrayList<>();

        // Создаем кнопки для каждой опции
        for (Object option : options) {
            String optionText = option.toString();
            String displayText = selectedOptions.contains(optionText) ? "✅ " + optionText : optionText;
            String callbackData = "multiselect:" + optionText;

            InlineKeyboardButton button = InlineKeyboardButton.builder()
                .text(displayText)
                .callbackData(callbackData)
                .build();

            currentRow.add(button);

            // Если достигли нужного количества столбцов или это последняя опция
            if (currentRow.size() == columnsCount) {
                keyboard.add(new ArrayList<>(currentRow));
                currentRow.clear();
            }
        }

        // Добавляем оставшиеся кнопки, если есть неполная строка
        if (!currentRow.isEmpty()) {
            keyboard.add(currentRow);
        }

        // Добавляем кнопку "Готово" в отдельную строку
        InlineKeyboardButton doneButton = InlineKeyboardButton.builder()
            .text("✅ Готово")
            .callbackData("multiselect:done")
            .build();

        keyboard.add(List.of(doneButton));

        return InlineKeyboardMarkup.builder()
            .keyboard(keyboard)
            .build();
    }

    protected void handleMultiSelectCallback(Update update, MultiSelectType type) {
        CallbackQuery callbackQuery = update.getCallbackQuery();
        String callbackData = callbackQuery.getData();
        long chatId = callbackQuery.getMessage().getChatId();
        Integer messageId = callbackQuery.getMessage().getMessageId();

        UserState userState = userStateRepository.get(chatId);
        UserState.CommandState commandState = userState.getOrCreateCommandState(getCommand());
        UserState.MultiSelect multiSelect = commandState.getMultiSelectParam(type);

        if (multiSelect == null) {
            return;
        }

        if (callbackData.startsWith("multiselect:")) {
            // Переключение опции
            String option = callbackData.substring("multiselect:".length());
            multiSelect.toggleOption(option);

            // Обновляем keyboard с новым состоянием
            Set<String> allOptions = getAllOptionsForType(type);
            InlineKeyboardMarkup newMarkup = createMultiSelectMarkup(
                allOptions, multiSelect.getSelectedOptions(), type.getColumnCount()
            );

            EditMessageText editText = new EditMessageText();
            editText.setChatId(String.valueOf(chatId));
            editText.setMessageId(messageId);
            editText.setText(multiSelect.getInitialMessage());
            editText.setReplyMarkup(newMarkup);
            executeMessage(editText);
        }
        // Отвечаем на callback query чтобы убрать индикатор загрузки
        AnswerCallbackQuery answerCallbackQuery = new AnswerCallbackQuery(callbackQuery.getId());
        executeMessage(answerCallbackQuery);
    }

    protected void showAlert(@Nullable CallbackQuery callbackQuery, long chatId, String messageText) {
        if (callbackQuery != null) {
            // Пытаемся показать popup alert
            AnswerCallbackQuery alertQuery = new AnswerCallbackQuery(callbackQuery.getId());
            alertQuery.setText(messageText);
            alertQuery.setShowAlert(true);
            executeMessage(alertQuery);
            return; // Успешно показали alert
        }
        // Показываем как обычное сообщение
        sendMessage(chatId, "⚠️ " + messageText);
    }

    protected Set<String> getAllOptionsForType(MultiSelectType type) {
        // Возвращаем все доступные опции для данного типа
        switch (type) {
            case SEAT_FILTER -> {
                return Arrays.stream(SeatFilter.values())
                    .map(SeatFilter::getButtonText)
                    .collect(Collectors.toSet());
            }
            default -> {
                return new HashSet<>();
            }
        }
    }

}