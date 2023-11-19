package net.osandman.rzdmonitoring.service;

public enum LayerId {
    ROUTE_ID("5827"),
    ROUTE_WITH_STATIONS("5804"),
    DETAIL_ID("5764");

    public final String value;

    LayerId(String value) {
        this.value = value;
    }
}
