package net.osandman.rzdmonitoring.entity;

/**
 * Код категории запроса.
 */
public enum LayerId {
    ROUTE_ID("5827"), // выбор маршрута (Получения списка поездов)
    ROUTE_WITH_STATIONS("5804"), // просмотр маршрута со всеми остановками (реализовано по-другому)
    DETAIL_ID("5764"); // детальная информация выбранному по поезду, список вагонов и свободных мест

    public final String code;

    LayerId(String code) {
        this.code = code;
    }
}
