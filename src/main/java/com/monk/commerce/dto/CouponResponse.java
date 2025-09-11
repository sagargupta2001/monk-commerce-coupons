package com.monk.commerce.dto;

import com.monk.commerce.entity.CouponType;

public record CouponResponse(
        Integer id,
        CouponType type,
        CouponDetails details
) {}