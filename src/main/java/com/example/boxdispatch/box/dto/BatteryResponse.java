package com.example.boxdispatch.box.dto;

public record BatteryResponse(
    String txref,
    int batteryPercentage
) {
}