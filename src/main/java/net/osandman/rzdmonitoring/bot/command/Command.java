package net.osandman.rzdmonitoring.bot.command;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;

@RequiredArgsConstructor
@Getter
public enum Command {

    START("/start", "\uD83C\uDFAC вывод всех команд", 1), // 🎬
    TICKETS("/tickets", "\uD83C\uDFAB создание задачи для отслеживания билетов", 2), // 🎫
    ROUTES("/routes", "\uD83D\uDE9D поиск маршрутов", 3), // 🚝
    STATIONS("/stations", "\uD83C\uDFEB поиск станций", 4), // 🏫
    TASKS("/tasks", "\uD83D\uDC40 действия с задачами", 5), // 👀
    ADM("/adm", "\uD83D\uDC41 админка", 6), // 👁
    UNKNOWN(null, "\uD83D\uDC7D неизвестная команда", 7), // 👽
    ;

    private final String commandStr;
    private final String desc;
    private final int order;

    public static Command getByCommandStr(String commandStr) {
        return Arrays.stream(values())
            .filter(command -> command.getCommandStr().equalsIgnoreCase(commandStr))
            .findAny()
            .orElse(UNKNOWN);
    }
}
