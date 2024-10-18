package net.osandman.rzdmonitoring.bot.command;

import lombok.RequiredArgsConstructor;
import net.osandman.rzdmonitoring.entity.UserState;
import net.osandman.rzdmonitoring.scheduler.MultiTaskScheduler;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Message;
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
    public String getCommand() {
        return CommandEnum.TASKS.getCommand();
    }

    @Override
    public void handleCommand(Update update) {
        Message message = update.getMessage();
        long chatId = message.getChatId();
        String messageText = message.getText();
        String userName = message.getChat().getFirstName() + " " + message.getChat().getLastName();

        log.info("Сообщение '{}' получено от пользователя {}, chatId={}", messageText, userName, chatId);

        UserState userState = userStateRepository.getOrCreate(chatId);
        UserState.CommandState commandState = userState.getOrCreateCommandState(getCommand()); // устанавливает команду если ее не было

        String allTasks = "Все";
        switch (commandState.getStep()) {
            case 1 -> { // начало команды
                StringBuilder tasks = new StringBuilder();
                Map<String, ScheduledFuture<?>> scheduledTasks = taskSchedulingConfig.getScheduledTasks();
                if (scheduledTasks != null && !scheduledTasks.isEmpty()) {
                    for (Map.Entry<String, ScheduledFuture<?>> futureEntry : scheduledTasks.entrySet()) {
                        tasks.append("✳ ").append(futureEntry.getKey()).append(System.lineSeparator());
                    }
//                    for (Thread thread : scheduledTasks.get(chatId)) {
//                        tasks.append("✳ ").append(thread.getName()).append(System.lineSeparator());
//                    }
                    sendMessage(chatId, "Запущены задачи: \n" + tasks);
                    List<String> taskNames = new ArrayList<>(scheduledTasks.keySet());
                    taskNames.add(allTasks);
                    sendButtons(chatId, "Удалить задачи", taskNames);
                } else {
                    sendMessage(chatId, "⚠ Задачи отсутствуют");
                }
                commandState.incrementStep();
            }
            case 2 -> {
                Map<String, ScheduledFuture<?>> scheduledTasks = taskSchedulingConfig.getScheduledTasks();
                if (messageText.equalsIgnoreCase(allTasks)) {
                    if (scheduledTasks != null && !scheduledTasks.isEmpty()) {
                        for (Map.Entry<String, ScheduledFuture<?>> futureEntry : scheduledTasks.entrySet()) {
                            taskSchedulingConfig.removeTask(futureEntry.getKey());
                        }
                    } else {
                        sendMessage(chatId, "⚠ Задачи отсутствуют");
                    }
                    sendMessage(chatId, "Все задачи удалены");
                } else {
                    taskSchedulingConfig.removeTask(messageText);
                    sendMessage(chatId, "Задача удалена");
                }
                userStateRepository.remove(chatId);
            }
        }
    }

    @Override
    public boolean canToShow() {
        return true;
    }
}