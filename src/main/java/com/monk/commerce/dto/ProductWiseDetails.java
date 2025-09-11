package com.monk.commerce.dto;

import java.time.LocalDate;

public record ProductWiseDetails(
        Integer productId,
        double discount,
        LocalDate expiryDate
) implements CouponDetails{}
