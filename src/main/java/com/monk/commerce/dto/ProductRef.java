package com.monk.commerce.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record ProductRef(
        @JsonProperty("product_id") int productId
) {}
