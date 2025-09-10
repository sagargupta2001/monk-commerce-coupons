package com.monk.commerce.dto;

public record DiscountedItem(
        Integer productId,
        Integer quantity,
        Double price,
        Double totalDiscount
) {}