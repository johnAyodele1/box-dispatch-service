package com.example.boxdispatch.box.dto;

import com.example.boxdispatch.box.BoxState;

import java.time.OffsetDateTime;
import java.util.UUID;

public record BoxResponse(
    UUID id,
    String txref,
    int weightLimitGrams,
    int batteryPercentage,
    BoxState state,
    OffsetDateTime createdAt,
    OffsetDateTime updatedAt
) {
}