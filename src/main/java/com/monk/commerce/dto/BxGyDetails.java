package com.monk.commerce.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public record BxGyDetails(
        @JsonProperty("buyProducts") List<ProductQuantity> buyProducts,
        @JsonProperty("getProducts") List<ProductQuantity> getProducts,
        @JsonProperty("buyQuantity") int buyQuantity,
        @JsonProperty("getQuantity") int getQuantity,
        @JsonProperty("repetitionLimit") int repetitionLimit
) {}
