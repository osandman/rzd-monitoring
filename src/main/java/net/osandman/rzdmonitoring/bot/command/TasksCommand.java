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
                Map<String, ScheduledFuture<?>> scheduledTasks = taskSchedulingConfig.getScheduledTasks();
                if (scheduledTasks != null && !scheduledTasks.isEmpty()) {
                    for (Map.Entry<String, ScheduledFuture<?>> futureEntry : scheduledTasks.entrySet()) {
                        tasks.append("✳ ").append(futureEntry.getKey()).append(System.lineSeparator());
                    }
                    sendMessage(command.chatId(), "Запущены задачи: \n" + tasks);
                    List<String> taskNames = new ArrayList<>(scheduledTasks.keySet());
                    taskNames.add(allTasks);
                    sendButtons(command.chatId(), "Удалить задачи", taskNames);
                } else {
                    sendMessage(command.chatId(), "⚠ Задачи отсутствуют");
                }
                command.state().incrementStep();
            }
            case 2 -> {
                Map<String, ScheduledFuture<?>> scheduledTasks = taskSchedulingConfig.getScheduledTasks();
                if (command.messageText().equalsIgnoreCase(allTasks)) {
                    if (scheduledTasks != null && !scheduledTasks.isEmpty()) {
                        for (Map.Entry<String, ScheduledFuture<?>> futureEntry : scheduledTasks.entrySet()) {
                            taskSchedulingConfig.removeTask(futureEntry.getKey());
                        }
                    } else {
                        sendMessage(command.chatId(), "⚠ Задачи отсутствуют");
                    }
                    sendMessage(command.chatId(), "Все задачи удалены");
                } else {
                    taskSchedulingConfig.removeTask(command.messageText());
                    sendMessage(command.chatId(), "Задача удалена");
                }
                userStateRepository.get(command.chatId()).deleteCommand(getCommand());
            }
        }
    }

    @Override
    public boolean canToShow() {
        return true;
    }
}