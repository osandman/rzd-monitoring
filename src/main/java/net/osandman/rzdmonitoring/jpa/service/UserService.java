package net.osandman.rzdmonitoring.jpa.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.osandman.rzdmonitoring.jpa.entity.User;
import net.osandman.rzdmonitoring.jpa.mapper.UserMapper;
import net.osandman.rzdmonitoring.jpa.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.time.ZonedDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    @Transactional
    public void createOrUpdate(Update update) {
        org.telegram.telegrambots.meta.api.objects.User telegramUser = extractUserFromUpdate(update);
        if (telegramUser == null) {
            log.warn("Не удалось определить пользователя для update = '{}'", update);
            return;
        }
        Long chatId = telegramUser.getId();
        userRepository.findByChatId(chatId)
            .ifPresentOrElse(
                entity -> {
                    userMapper.updateByTelegramUser(entity, telegramUser);
                    entity.setUpdatedAt(ZonedDateTime.now());
                    userRepository.save(entity);
                },
                () -> {
                    User newUser = userMapper.toEntity(telegramUser);
                    userRepository.save(newUser);
                    log.info("Создан новый пользователь с chatId={}, username={}", chatId, telegramUser.getUserName());
                }
            );
    }

    /**
     * Извлекаем User из разных типов Update
     */
    private org.telegram.telegrambots.meta.api.objects.User extractUserFromUpdate(Update update) {
        if (update.hasMessage() && update.getMessage().getFrom() != null) {
            return update.getMessage().getFrom();
        } else if (update.hasCallbackQuery() && update.getCallbackQuery().getFrom() != null) {
            return update.getCallbackQuery().getFrom();
        } else if (update.hasInlineQuery()) {
            return update.getInlineQuery().getFrom();
        } else if (update.hasChosenInlineQuery()) {
            return update.getChosenInlineQuery().getFrom();
        } else if (update.hasChannelPost() && update.getChannelPost().getFrom() != null) {
            return update.getChannelPost().getFrom();
        } else if (update.hasEditedMessage() && update.getEditedMessage().getFrom() != null) {
            return update.getEditedMessage().getFrom();
        }
        return null;
    }
}
