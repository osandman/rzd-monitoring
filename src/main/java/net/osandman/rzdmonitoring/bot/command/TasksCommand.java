package net.osandman.rzdmonitoring.bot.command;

import lombok.RequiredArgsConstructor;
import net.osandman.rzdmonitoring.scheduler.MultiTaskScheduler;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;

@Component
@RequiredArgsConstructor
public class TasksCommand extends AbstractTelegramCommand implements ITelegramCommand {

    private final MultiTaskScheduler taskSchedulingConfig;

    @Override
    public Command getCommand() {
        return Command.TASKS;
    }

    @Override
    public void handleCommand(Update update) {
        CommandContext command = buildCommandContext(update, getCommand());
        String allTasks = "Все";
        switch (command.state().getStep()) {
            case 1 -> { // начало команды
                StringBuilder tasks = new StringBuilder();
                Map<Long, Map<String, ScheduledFuture<?>>> scheduledTasks = taskSchedulingConfig.getScheduledTasks();
                Map<String, ScheduledFuture<?>> taskMap = scheduledTasks.get(command.chatId());
                if (taskMap != null && !taskMap.isEmpty()) {
                    for (Map.Entry<String, ScheduledFuture<?>> futureEntry : taskMap.entrySet()) {
                        tasks.append("✳ ").append(futureEntry.getKey()).append(System.lineSeparator());
                    }
                    sendMessage(command.chatId(), "Запущены задачи: \n" + tasks);
                    List<String> taskNames = new ArrayList<>(taskMap.keySet());
                    taskNames.add(allTasks);
                    sendButtons(command.chatId(), "Удалить задачи", taskNames);
                } else {
                    sendMessage(command.chatId(), "\uD83D\uDDD1\uFE0F Задачи отсутствуют"); // 🗑️
                }
                command.state().incrementStep();
            }
            case 2 -> {
                Map<Long, Map<String, ScheduledFuture<?>>> scheduledTasks = taskSchedulingConfig.getScheduledTasks();
                Map<String, ScheduledFuture<?>> taskMap = scheduledTasks.get(command.chatId());
                if (allTasks.equalsIgnoreCase(command.messageText())) {
                    Integer removedCount = taskSchedulingConfig.removeTasks(command.chatId());
                    if (removedCount == null || removedCount == 0) {
                        sendMessage(command.chatId(), "\uD83D\uDDD1\uFE0F Задачи отсутствуют"); // 🗑️
                    } else {
                        sendMessage(command.chatId(), "Все (%d) задачи удалены".formatted(removedCount));
                    }
                } else if (taskMap.containsKey(command.messageText())) {
                    Boolean check = taskSchedulingConfig.removeTask(command.chatId(), command.messageText());
                    if (check == null) {
                        sendMessage(command.chatId(), "\uD83D\uDDD1\uFE0F Задачи отсутствуют"); // 🗑️
                    } else if (check) {
                        sendMessage(command.chatId(), "Задача '%s' удалена".formatted(command.messageText()));
                    } else {
                        sendMessage(command.chatId(), "Ошибка при удалении задачи '%s'".formatted(command.messageText()));
                    }
                } else {
                    sendMessage(command.chatId(), "Задача '%s' не существует".formatted(command.messageText()));
                }

//                if (command.messageText().equalsIgnoreCase(allTasks)) {
//                    if (taskMap != null && !scheduledTasks.isEmpty()) {
//                        for (Map.Entry<String, ScheduledFuture<?>> futureEntry : taskMap.entrySet()) {
//                            taskSchedulingConfig.removeTask(command.chatId(), futureEntry.getKey());
//                        }
//                    } else {
//                        sendMessage(command.chatId(), "\uD83D\uDDD1\uFE0F Задачи отсутствуют"); // 🗑️
//                    }
//                    sendMessage(command.chatId(), "Все задачи удалены");
//                } else {
//                    taskSchedulingConfig.removeTask(command.messageText());
//                    sendMessage(command.chatId(), "Задача удалена");
//                }
                userStateRepository.get(command.chatId()).deleteCommand(getCommand());
            }
        }
    }

    @Override
    public boolean canToShow() {
        return true;
    }
}