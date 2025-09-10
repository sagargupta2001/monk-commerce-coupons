package com.monk.commerce.dto;

import java.util.List;

public record ApplyCouponResponse(
        List<DiscountedItem> items,
        Double totalPrice,
        Double totalDiscount,
        Double finalPrice
) {}