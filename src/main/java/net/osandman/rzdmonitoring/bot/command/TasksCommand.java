package net.osandman.rzdmonitoring.bot.command;

import lombok.RequiredArgsConstructor;
import net.osandman.rzdmonitoring.scheduler.MultiTaskScheduler;
import net.osandman.rzdmonitoring.scheduler.State;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static net.osandman.rzdmonitoring.bot.command.Buttons.CHANGE_INTERVAL;
import static net.osandman.rzdmonitoring.bot.command.Buttons.DELETE;
import static net.osandman.rzdmonitoring.bot.command.Buttons.DELETE_ALL;
import static net.osandman.rzdmonitoring.bot.command.Buttons.START_ALL;
import static net.osandman.rzdmonitoring.bot.command.Buttons.STOP_ALL;

@Component
@RequiredArgsConstructor
public class TasksCommand extends AbstractTelegramCommand implements ITelegramCommand {

    private final MultiTaskScheduler taskScheduler;

    public static final String DELETE_ICON1 = "\uD83D\uDDD1"; // 🗑
    public static final String DELETE_ICON2 = "\uD83D\uDDD1\uFE0F"; // 🗑️
    public static final String EMPTY_ICON = "\uD83D\uDCED"; // 📭️

    @Override
    public Command getCommand() {
        return Command.TASKS;
    }

    @Override
    public void handleCommand(Update update) {
        CommandContext command = buildCommandContext(update, getCommand());
        Map<String, MultiTaskScheduler.TaskInfo> taskMap = taskScheduler.getScheduledTasks().get(command.chatId());
        switch (command.state().getStep()) {
            case 1 -> { // приглашение к вводу команды
                beginHandle(command, taskMap);
                command.state().incrementStep();
            }
            case 2 -> { // обработка команды
                switch (command.messageText()) {
                    case DELETE -> {
                        List<String> taskNames = new ArrayList<>(taskMap.keySet());
                        taskNames.add(DELETE_ALL);
                        sendButtons(
                            command.chatId(), "Выберите задачу или '%s' для удаления:".formatted(DELETE_ALL), taskNames
                        );
                        command.state().setStep(3);
                    }
                    case START_ALL, STOP_ALL -> {
                        State toState = (taskMap.entrySet().iterator().next().getValue().getState() == State.ACTIVE)
                            ? State.PAUSED
                            : State.ACTIVE;
                        taskScheduler.changeState(toState);
                        List<String> buttons = buildButtons(taskMap);
                        sendButtons(command.chatId(), "Текущий статус задач: '%s'".formatted(toState), buttons);
                    }
                    case CHANGE_INTERVAL -> {
                        List<Integer> buttons = List.of(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);
                        sendButtons(command.chatId(), "Укажите время в минутах", buttons);
                        command.state().setStep(4);
                    }
                }
            }
            case 3 -> { // удалить задачи
                deleteTasks(command, taskMap);
            }
            case 4 -> { // изменить интервал
                taskScheduler.changeInterval(Long.parseLong(command.messageText()));
                long newInterval = taskMap.entrySet().iterator().next().getValue().getInterval();
                List<String> buttons = buildButtons(taskMap);
                sendButtons(
                    command.chatId(), "Интервал мониторинга изменен на %d минут".formatted(newInterval), buttons
                );
                command.state().setStep(2);
            }
        }
    }

    private void beginHandle(CommandContext command, Map<String, MultiTaskScheduler.TaskInfo> taskMap) {
        StringBuilder tasks = new StringBuilder();
        if (taskMap != null && !taskMap.isEmpty()) {
            for (Map.Entry<String, MultiTaskScheduler.TaskInfo> taskEntry : taskMap.entrySet()) {
                tasks.append("✳ ").append(taskEntry.getKey()).append("-")
                    .append(taskEntry.getValue().getState()).append(System.lineSeparator());
            }
            sendMessage(command.chatId(), "Текущие задачи: \n" + tasks);
            List<String> buttons = buildButtons(taskMap);
            sendButtons(command.chatId(), "Выберите действия с задачами:", buttons);
        } else {
            sendMessage(command.chatId(), EMPTY_ICON + " Задачи отсутствуют");
        }
    }

    private static List<String> buildButtons(Map<String, MultiTaskScheduler.TaskInfo> taskMap) {
        String startOrStop = taskMap.entrySet().iterator().next().getValue().getState() == State.ACTIVE
            ? STOP_ALL : START_ALL;
        return List.of(DELETE, startOrStop, CHANGE_INTERVAL);
    }

    private void deleteTasks(CommandContext command, Map<String, MultiTaskScheduler.TaskInfo> taskMap) {
        String messageText = command.messageText();
        long chatId = command.chatId();
        if (DELETE_ALL.equalsIgnoreCase(messageText)) {
            Integer removedCount = taskScheduler.removeAllTasks(chatId);
            if (removedCount == null || removedCount == 0) {
                sendMessage(chatId, EMPTY_ICON + " Задачи отсутствуют");
            } else {
                sendMessage(chatId, DELETE_ICON2 + " Все (%d) задачи удалены".formatted(removedCount));
            }
            userStateRepository.get(command.chatId()).deleteCommand(getCommand());
            return;
        }
        if (taskMap.containsKey(messageText)) {
            Boolean check = taskScheduler.removeTask(chatId, messageText);
            if (check == null) {
                sendMessage(chatId, EMPTY_ICON + " Задачи отсутствуют");
            } else if (check) {
                sendMessage(chatId, DELETE_ICON1 + " Задача '%s' удалена".formatted(messageText));
            } else {
                sendMessage(chatId, "Ошибка при удалении задачи '%s'".formatted(messageText));
            }
        } else {
            sendMessage(chatId, "Задача '%s' не существует".formatted(messageText));
        }
        beginHandle(command, taskMap);
        command.state().setStep(2);
    }

    @Override
    public boolean canToShow() {
        return true;
    }
}