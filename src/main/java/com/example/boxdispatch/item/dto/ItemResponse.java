package com.example.boxdispatch.item.dto;

import java.util.UUID;

public record ItemResponse(
    UUID id,
    String name,
    int weightGrams,
    String code
) {
}