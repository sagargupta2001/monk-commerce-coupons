package com.monk.commerce.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record ProductQuantity(
        @JsonProperty("productId") Integer productId,
        @JsonProperty("quantity") Integer quantity
) {}
