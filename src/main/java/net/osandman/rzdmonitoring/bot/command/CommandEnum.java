package net.osandman.rzdmonitoring.bot.command;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum CommandEnum {
    START("/start"),
    TICKETS("/tickets"),
    TASKS("/tasks"),
    ROUTES("/routes");

    private final String command;
}
