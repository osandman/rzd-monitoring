package net.osandman.rzdmonitoring.bot.command;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;

@RequiredArgsConstructor
@Getter
public enum Command {
    START("/start", "\uD83C\uDFAC вывод всех команд"), // 🎬
    TICKETS("/tickets", "\uD83C\uDFAB создание задачи для мониторинга билетов"), // 🎫
    TASKS("/tasks", "\uD83D\uDC40 просмотр задач"), // 👀
    ROUTES("/routes", "\uD83D\uDE9D поиск маршрутов"), // 🚝
    STATIONS("/stations", "\uD83C\uDFEB поиск станций"), // 🏫
    UNKNOWN(null, "\uD83D\uDC7D неизвестная команда"); // 👽

    private final String commandStr;
    private final String desc;

    public static Command getByCommandStr(String commandStr) {
        return Arrays.stream(values())
            .filter(command -> command.getCommandStr().equalsIgnoreCase(commandStr))
            .findAny()
            .orElse(UNKNOWN);
    }
}
