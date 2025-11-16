package net.osandman.rzdmonitoring.jpa.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;

import static org.springframework.util.StringUtils.hasText;

@Getter
@RequiredArgsConstructor
public enum UserStatus {

    MEMBER("member", "Участник чата или подписан на бота"),
    KICKED("kicked", "Пользователь заблокировал бота или был исключен из чата"),
    LEFT("left", "Пользователь покинул чат или удалил диалог с ботом"),
    RESTRICTED("restricted", "Пользователь ограничен в правах (только для групп/каналов)"),
    PENDING("pending", "Пользователь ожидает подтверждения"),
    UNKNOWN("unknown", "Неизвестный статус");

    private final String telegramStatus;
    private final String description;

    /**
     * Парсит статус из строки Telegram API
     */
    @NonNull
    public static UserStatus fromTelegramStatus(String statusStr) {
        if (!hasText(statusStr)) {
            return UNKNOWN;
        }
        String normalized = statusStr.toLowerCase().trim();
        return switch (normalized) {
            case "member", "administrator", "creator" -> MEMBER;
            case "kicked" -> KICKED;
            case "left" -> LEFT;
            case "restricted" -> RESTRICTED;
            default -> UNKNOWN;
        };
    }

    /**
     * Проверяет активен ли пользователь
     */
    public boolean isActive() {
        return this == MEMBER;
    }

    /**
     * Проверяет, вышел ли пользователь из бота
     */
    public boolean isUserLeft() {
        return this == KICKED || this == LEFT;
    }
}
