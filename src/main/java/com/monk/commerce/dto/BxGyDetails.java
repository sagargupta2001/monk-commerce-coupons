package com.monk.commerce.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDate;
import java.util.List;

public record BxGyDetails(
        @JsonProperty("buyProducts") List<ProductQuantity> buyProducts,
        @JsonProperty("getProducts") List<ProductQuantity> getProducts,
        @JsonProperty("repetitionLimit") int repetitionLimit,
        LocalDate expiryDate
) implements CouponDetails {}
