package com.monk.commerce.dto;

import com.monk.commerce.entity.CouponType;

public record CouponRequest(
        CouponType type,
        Object details
) {}