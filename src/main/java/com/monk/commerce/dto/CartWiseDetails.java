package com.monk.commerce.dto;

import java.time.LocalDate;

public record CartWiseDetails(
        double threshold,
        double discount,
        LocalDate expiryDate
) implements CouponDetails {}

