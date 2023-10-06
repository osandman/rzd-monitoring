package net.osandman.rzdmonitoring.bot.command;

public enum CommandEnum {
    START("/start"),
    TICKETS("/tickets"),
    ROUTES("/routes");

    final String name;
    CommandEnum(String name) {
        this.name = name;
    }
}
