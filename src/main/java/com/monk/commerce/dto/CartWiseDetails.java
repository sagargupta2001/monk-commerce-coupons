package com.monk.commerce.dto;

public record CartWiseDetails(
        double threshold,
        double discount
) implements CouponDetails {}

