package com.monk.commerce.dto;

public record ProductWiseDetails(
        Integer productId,
        double discount
) implements CouponDetails{}
