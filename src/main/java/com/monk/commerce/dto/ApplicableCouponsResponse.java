package com.monk.commerce.dto;

import java.util.List;

public record ApplicableCouponsResponse(
        List<ApplicableCoupon> applicableCoupons
) {}