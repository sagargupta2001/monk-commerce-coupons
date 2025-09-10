package com.monk.commerce.dto;

import com.monk.commerce.entity.CouponType;

public record ApplicableCoupon(
        Integer couponId,
        CouponType type,
        Double discount
) {}