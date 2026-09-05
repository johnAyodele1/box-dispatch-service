package com.example.boxdispatch.box.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateBoxRequest(

    @NotBlank
    @Size(max = 20)
    String txref,

    @Min(1)
    @Max(500)
    int weightLimitGrams,

    @Min(0)
    @Max(100)
    int batteryPercentage

) {
}