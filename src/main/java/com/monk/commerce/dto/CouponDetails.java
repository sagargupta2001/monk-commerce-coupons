package com.monk.commerce.dto;

import java.time.LocalDate;

public sealed interface CouponDetails
        permits CartWiseDetails, ProductWiseDetails, BxGyDetails {
    LocalDate expiryDate();
}