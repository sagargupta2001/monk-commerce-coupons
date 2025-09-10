package com.monk.commerce.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record ProductWiseDetails(
        @JsonProperty("productId") Integer productId,
        @JsonProperty("discount") double discount
) {}
