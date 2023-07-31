package com.etsubu.stonksbot.scheduler.omxnordic.Model;

public record Transaction(String type, String currency, int volume, double price) {
    public double totalSum() { return volume * price; }

    public String key() { return type + currency; }
}
