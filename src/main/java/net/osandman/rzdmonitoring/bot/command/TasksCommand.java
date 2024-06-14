package net.osandman.rzdmonitoring.bot.command;

import net.osandman.rzdmonitoring.entity.UserState;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.List;

@Component
public class TasksCommand extends AbstractTelegramCommand implements ITelegramCommand {

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
                if (threads.get(chatId) != null) {
                    for (Thread thread : threads.get(chatId)) {
                        tasks.append("✳ ").append(thread.getName()).append(System.lineSeparator());
                    }
                    sendMessage(chatId, "Запущены задачи: \n" + tasks);
                    sendButtons(chatId, "Удалить задачи", List.of(allTasks));
                } else {
                    sendMessage(chatId, "⚠ Задачи отсутствуют");
                }
                commandState.incrementStep();
            }
            case 2 -> {
                if (messageText.equalsIgnoreCase(allTasks)) {
                    // TODO доработать шедулер и создать ThreadFactory, сейчас interrupt() с exception
                    for (Thread thread : threads.get(chatId)) {
                        try {
                            thread.interrupt();
                            thread.join();
                        } catch (InterruptedException e) {
                            log.error("Ошибка при удалении потока", e);
                            sendMessage(chatId, "Ошибка при удалении задачи '%s'".formatted(thread.getName()));
                        }
                    }
                    sendMessage(chatId, "Все задачи удалены");
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