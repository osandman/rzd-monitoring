package net.osandman.rzdmonitoring.bot.command;

import lombok.RequiredArgsConstructor;
import net.osandman.rzdmonitoring.scheduler.MultiTaskScheduler;
import net.osandman.rzdmonitoring.scheduler.TicketsTask;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static net.osandman.rzdmonitoring.bot.command.Buttons.CHANGE_INTERVAL;
import static net.osandman.rzdmonitoring.bot.command.Buttons.DELETE;
import static net.osandman.rzdmonitoring.bot.command.Buttons.DELETE_ALL;
import static net.osandman.rzdmonitoring.util.Utils.getFirstWord;

@Component
@RequiredArgsConstructor
public class AdminCommand extends AbstractTelegramCommand {

    @Value("${bot.chat-id}")
    private Long adminChatId;

    private final MultiTaskScheduler taskScheduler;

    public static final String DELETE_ICON1 = "\uD83D\uDDD1"; // 🗑
    public static final String DELETE_ICON2 = "\uD83D\uDDD1\uFE0F"; // 🗑️
    public static final String EMPTY_ICON = "\uD83D\uDCED"; // 📭️

    @Override
    public Command getCommand() {
        return Command.ADM;
    }

    @Override
    public void handleCommand(Update update) {
        CommandContext command = buildCommandContext(update, getCommand());
        if (command.chatId() != adminChatId) {
            sendMessage(command.chatId(), "У вас нет прав для выполнения данной команды", true);
            return;
        }
        Map<Long, Map<String, MultiTaskScheduler.TaskInfo>> scheduledTasks = taskScheduler.getScheduledTasks();

        List<TicketsTask> ticketsTasks = new ArrayList<>();
        for (var entry : scheduledTasks.entrySet()) {
            Map<String, MultiTaskScheduler.TaskInfo> value = entry.getValue();
            for (MultiTaskScheduler.TaskInfo taskInfo : value.values()) {
                ticketsTasks.add(taskInfo.getTicketsTask());
            }
        }
        command.state().setAdditionalObjects(Map.of(ParamType.TASKS, ticketsTasks));

        switch (command.state().getStep()) {
            case 1 -> { // приглашение к вводу команды
                beginHandle(command);
                command.state().incrementStep();
            }
            case 2 -> { // обработка команды
                switch (command.messageText()) {
                    case DELETE -> {
                        List<String> taskNames = new ArrayList<>();
                        StringBuilder task = new StringBuilder();
                        List<TicketsTask> tasks = command.state().getAdditionalObject(
                            ParamType.TASKS, TicketsTask.class
                        );
                        tasks.forEach(ticketsTask -> {
                                task
                                    .append(ticketsTask.taskId()).append(" ")
                                    .append("chatId=").append(ticketsTask.chatId()).append(" ")
                                    .append(ticketsTask.prettyString());
                                taskNames.add(task.toString());
                                task.setLength(0);
                            }

                        );
                        taskNames.add(DELETE_ALL);
                        sendButtons(
                            command.chatId(), "Выберите задачу или '%s' для удаления:".formatted(DELETE_ALL),
                            taskNames,
                            1
                        );
                        command.state().setStep(3);
                    }
                    case CHANGE_INTERVAL -> {
                        List<Integer> buttons = List.of(5, 6, 7, 8, 9, 10, 15, 20, 30);
                        sendButtons(command.chatId(), "Укажите время в минутах", buttons);
                        command.state().setStep(4);
                    }
                }
            }
            case 3 -> { // удалить задачи
                deleteTasks(command);
                beginHandle(command);
                command.state().setStep(2);
            }
            case 4 -> { // изменить интервал
                long newInterval = taskScheduler.changeInterval(Long.parseLong(command.messageText()));
                List<String> buttons = buildButtons();
                sendButtons(
                    command.chatId(),
                    "Интервал мониторинга изменен для всех задач на %d минут".formatted(newInterval),
                    buttons
                );
                command.state().setStep(2);
            }
        }
    }

    private void beginHandle(
        CommandContext command
    ) {
        Map<Long, Map<String, MultiTaskScheduler.TaskInfo>> scheduledTasks = taskScheduler.getScheduledTasks();
        if (scheduledTasks.isEmpty()) {
            sendMessage(command.chatId(), EMPTY_ICON + " Задачи отсутствуют", true);
            return;
        }
        StringBuilder tasks = new StringBuilder();
        for (Map.Entry<Long, Map<String, MultiTaskScheduler.TaskInfo>> entry : scheduledTasks.entrySet()) {
            Map<String, MultiTaskScheduler.TaskInfo> value = entry.getValue();
            for (MultiTaskScheduler.TaskInfo taskInfo : value.values()) {
                tasks.append("✳ ")
                    .append(taskInfo.getTicketsTask().taskId()).append(" ")
                    .append("chatId=").append(entry.getKey()).append(" ")
                    .append("userName=").append(taskInfo.getTicketsTask().userName()).append(", ")
                    .append(taskInfo.getTicketsTask().prettyString()).append(" ")
                    .append(taskInfo.getState()).append(" ")
                    .append("интервал ").append(taskInfo.getInterval())
                    .append(System.lineSeparator());
            }
        }
        sendMessage(command.chatId(), "Текущие задачи: \n" + tasks, true);
        List<String> buttons = buildButtons();
        sendButtons(command.chatId(), "Выберите действия с задачами:", buttons);
    }

    private static List<String> buildButtons() {
        return List.of(DELETE, CHANGE_INTERVAL);
    }

    private void deleteTasks(CommandContext command) {
        String messageText = command.messageText();
        long chatId = command.chatId();
        List<TicketsTask> ticketsTasks = command.state().getAdditionalObject(ParamType.TASKS, TicketsTask.class);

        if (DELETE_ALL.equalsIgnoreCase(messageText)) {
            Integer removedCount = taskScheduler.removeAllTasks();
            if (removedCount == null || removedCount == 0) {
                sendMessage(chatId, EMPTY_ICON + " Задачи отсутствуют");
            } else {
                sendMessage(chatId, DELETE_ICON2 + " Все (%d) задачи удалены".formatted(removedCount));
                ticketsTasks.stream().map(TicketsTask::chatId).distinct()
                    .forEach(userChatId ->
                        sendMessage(userChatId, "⚠️Все задачи удалены администратором", true)
                    );
            }
            userStateRepository.get(command.chatId()).deleteCommand(getCommand());
            return;
        }

        String taskId = getFirstWord(messageText);
        TicketsTask taskForRemove = ticketsTasks.stream()
            .filter(ticketsTask -> ticketsTask.taskId().equals(taskId))
            .findFirst()
            .orElse(null);

        if (taskForRemove == null) {
            sendMessage(chatId, "Задача с taskId=%s не найдена".formatted(taskId));
            return;
        }

        Boolean check = taskScheduler.removeTask(taskForRemove.chatId(), taskId);
        if (check == null) {
            sendMessage(chatId, EMPTY_ICON + " Задачи отсутствуют");
            userStateRepository.get(command.chatId()).deleteCommand(getCommand());
        } else if (check) {
            sendMessage(chatId, DELETE_ICON1 + " Задача с taskId=%s удалена".formatted(taskId));
            sendMessage(
                taskForRemove.chatId(),
                "⚠️ Задача '%s' удалена администратором".formatted(taskForRemove.prettyString()),
                true
            );
        } else {
            sendMessage(chatId, "Ошибка при удалении задачи с taskId=%s".formatted(taskId));
        }

    }

    @Override
    public boolean canToShow() {
        return false;
    }
}