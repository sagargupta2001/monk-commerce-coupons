package com.monk.commerce.dto;

public record CartItem(
        Integer productId,
        Integer quantity,
        Double price
) {}