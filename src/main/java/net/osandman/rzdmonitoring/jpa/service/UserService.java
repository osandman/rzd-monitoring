package net.osandman.rzdmonitoring.jpa.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.osandman.rzdmonitoring.jpa.entity.User;
import net.osandman.rzdmonitoring.jpa.entity.UserStatus;
import net.osandman.rzdmonitoring.jpa.mapper.UserMapper;
import net.osandman.rzdmonitoring.jpa.repository.UserRepository;
import net.osandman.rzdmonitoring.scheduler.MultiTaskScheduler;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.telegram.telegrambots.meta.api.objects.ChatMemberUpdated;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.time.ZonedDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final MultiTaskScheduler multiTaskScheduler;

    @Transactional
    public void createOrUpdate(Update update) {
        UserInfo userInfo = extractUserFromUpdate(update);
        if (userInfo.telegramUser() == null) {
            log.warn("Не удалось определить пользователя для update с updateId={}", update.getUpdateId());
            return;
        }
        Long chatId = userInfo.telegramUser().getId();
        userRepository.findByChatId(chatId)
            .ifPresentOrElse(
                entity -> updateExistingUser(entity, userInfo),
                () -> createNewUser(userInfo, chatId)
            );
    }

    /**
     * Обновляет существующего пользователя
     */
    private void updateExistingUser(User entity, UserInfo userInfo) {
        UserStatus oldStatus = entity.getStatus();
        org.telegram.telegrambots.meta.api.objects.User telegramUser = userInfo.telegramUser();
        UserStatus newStatus = userInfo.userStatus();

        // Обновляем данные пользователя
        userMapper.updateByTelegramUser(entity, telegramUser);
        entity.setStatus(newStatus);
        entity.setActive(newStatus.isActive());
        entity.setUpdatedAt(ZonedDateTime.now());

        userRepository.save(entity);

        // При изменении статуса
        if (oldStatus != newStatus) {
            logStatusChange(entity.getChatId(), oldStatus, newStatus);
            if (newStatus.isUserLeft()) {
                log.warn("❌ Удаляем все задачи для пользователя {}", entity.getChatId());
                multiTaskScheduler.removeAllTasksByChatId(entity.getChatId());
            }
        }

        log.debug("Пользователь обновлен: chatId={}, username={}, status={}",
            entity.getChatId(), entity.getUsername(), newStatus);
    }

    /**
     * Создает нового пользователя
     */
    private void createNewUser(UserInfo userInfo, Long chatId) {
        org.telegram.telegrambots.meta.api.objects.User telegramUser = userInfo.telegramUser();
        UserStatus status = userInfo.userStatus();

        User newUser = userMapper.toEntity(telegramUser);
        newUser.setStatus(status);
        newUser.setActive(status.isActive());
        userRepository.save(newUser);

        log.info("Создан новый пользователь: chatId={}, username={}, status={}",
            chatId, telegramUser.getUserName(), status);
    }

    /**
     * Логирует изменение статуса
     */
    private void logStatusChange(Long chatId, UserStatus oldStatus, UserStatus newStatus) {
        if (newStatus.isUserLeft()) {
            log.warn("🚫 Пользователь {} покинул/заблокировал бота: {} → {}", chatId, oldStatus, newStatus);
        } else if (oldStatus != null && oldStatus.isUserLeft() && newStatus.isActive()) {
            log.info("✅ Пользователь {} снова появился в боте: {} → {}", chatId, oldStatus, newStatus);
        } else {
            log.info("👤 У пользователя {} изменился статус {} → {}", chatId, oldStatus, newStatus);
        }
    }

    /**
     * Извлекаем User и его статус из разных типов Update
     */
    private UserInfo extractUserFromUpdate(Update update) {
        // 1. Обычное сообщение
        if (update.hasMessage() && update.getMessage().getFrom() != null) {
            return new UserInfo(update.getMessage().getFrom(), UserStatus.MEMBER);
        }

        // 2. Callback query (нажатие на inline кнопку)
        if (update.hasCallbackQuery() && update.getCallbackQuery().getFrom() != null) {
            return new UserInfo(update.getCallbackQuery().getFrom(), UserStatus.MEMBER);
        }

        // 3. Отредактированное сообщение
        if (update.hasEditedMessage() && update.getEditedMessage().getFrom() != null) {
            return new UserInfo(update.getEditedMessage().getFrom(), UserStatus.MEMBER);
        }

        // 4. Inline query
        if (update.hasInlineQuery()) {
            return new UserInfo(update.getInlineQuery().getFrom(), UserStatus.MEMBER);
        }

        // 5. ChatMemberUpdated (пользователь заблокировал/разблокировал бота)
        if (update.hasMyChatMember()) {
            ChatMemberUpdated myChatMember = update.getMyChatMember();
            if (myChatMember != null && myChatMember.getFrom() != null) {
                String oldStatusStr = myChatMember.getOldChatMember() != null ?
                    myChatMember.getOldChatMember().getStatus() : null;
                String newStatusStr = myChatMember.getNewChatMember() != null ?
                    myChatMember.getNewChatMember().getStatus() : null;
                UserStatus userStatus = UserStatus.fromTelegramStatus(newStatusStr);
                log.info("myChatMember событие: пользователь {} изменил статус {} → {} ({})",
                    myChatMember.getFrom().getId(),
                    oldStatusStr,
                    newStatusStr,
                    userStatus);
                return new UserInfo(myChatMember.getFrom(), userStatus);
            }
        }

        // 6. ChatMember (изменение статуса участника чата)
        if (update.hasChatMember()) {
            ChatMemberUpdated chatMember = update.getChatMember();
            if (chatMember != null && chatMember.getFrom() != null) {
                String newStatusStr = chatMember.getNewChatMember() != null ?
                    chatMember.getNewChatMember().getStatus() : null;
                UserStatus userStatus = UserStatus.fromTelegramStatus(newStatusStr);
                return new UserInfo(chatMember.getFrom(), userStatus);
            }
        }

        // 7. PreCheckoutQuery
        if (update.hasPreCheckoutQuery() && update.getPreCheckoutQuery().getFrom() != null) {
            return new UserInfo(update.getPreCheckoutQuery().getFrom(), UserStatus.MEMBER);
        }

        // 8. ShippingQuery
        if (update.hasShippingQuery() && update.getShippingQuery().getFrom() != null) {
            return new UserInfo(update.getShippingQuery().getFrom(), UserStatus.MEMBER);
        }

        // 9. PollAnswer
        if (update.hasPollAnswer() && update.getPollAnswer().getUser() != null) {
            return new UserInfo(update.getPollAnswer().getUser(), UserStatus.MEMBER);
        }

        // 10. ChatJoinRequest
        if (update.hasChatJoinRequest() && update.getChatJoinRequest().getUser() != null) {
            return new UserInfo(update.getChatJoinRequest().getUser(), UserStatus.PENDING);
        }

        return new UserInfo(null, UserStatus.UNKNOWN);
    }

    record UserInfo(
        org.telegram.telegrambots.meta.api.objects.User telegramUser,
        UserStatus userStatus
    ) {
    }
}
