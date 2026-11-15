package com.bulbashop;

/** Баланс, который знает клиент (обновляется пакетом с сервера). */
public final class ClientState {
    public static volatile long balance = 0;

    private ClientState() {}
}
