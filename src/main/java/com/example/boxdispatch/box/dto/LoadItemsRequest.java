package com.example.boxdispatch.box.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;

import java.util.List;

public record LoadItemsRequest(

    @NotEmpty
    @Valid
    List<LoadItemRequest> items

) {

    public record LoadItemRequest(

        @NotBlank
        @Pattern(
            regexp = "^[A-Za-z0-9_-]+$",
            message = "name may contain only letters, numbers, hyphen, and underscore"
        )
        String name,

        @Min(1)
        int weightGrams,

        @NotBlank
        @Pattern(
            regexp = "^[A-Z0-9_]+$",
            message = "code may contain only uppercase letters, numbers, and underscore"
        )
        String code

    ) {
    }
}