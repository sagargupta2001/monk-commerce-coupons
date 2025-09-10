package com.monk.commerce.dto;


public record CartItem(
        Integer productId,
        int quantity,
        double price
) {}