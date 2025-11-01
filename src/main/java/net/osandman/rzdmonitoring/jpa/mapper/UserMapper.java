package net.osandman.rzdmonitoring.jpa.mapper;

import net.osandman.rzdmonitoring.jpa.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValueCheckStrategy;
import org.mapstruct.ReportingPolicy;

import java.time.ZonedDateTime;

@Mapper(
    componentModel = MappingConstants.ComponentModel.SPRING,
    nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS,
    unmappedTargetPolicy = ReportingPolicy.ERROR,
    imports = {ZonedDateTime.class}
)
public interface UserMapper {

    @Mapping(target = "username", source = "userName")
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "active", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "chatId", source = "id")
    User toEntity(org.telegram.telegrambots.meta.api.objects.User telegramUser);

    @Mapping(target = "username", source = "userName")
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "active", ignore = true)
    @Mapping(target = "createdAt", expression = "java(ZonedDateTime.now())")
    @Mapping(target = "chatId", source = "id")
    void updateByTelegramUser(
        @MappingTarget User user,
        org.telegram.telegrambots.meta.api.objects.User telegramUser
    );
}
