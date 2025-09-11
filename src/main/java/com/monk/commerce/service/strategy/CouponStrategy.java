package com.monk.commerce.service.strategy;

import com.monk.commerce.dto.*;
import com.monk.commerce.entity.CouponType;

import java.time.LocalDate;

public interface CouponStrategy {

    boolean isApplicable(CartRequest cart, CouponResponse coupon);

    double calculateDiscount(CartRequest cart, CouponResponse coupon);

    ApplyCouponResponse applyCoupon(CartRequest cart, CouponResponse coupon);

    CouponType getType();

    default boolean isExpired(CouponResponse coupon) {
        LocalDate expiry = coupon.details().expiryDate();
        return expiry != null && expiry.isBefore(LocalDate.now());
    }
}
