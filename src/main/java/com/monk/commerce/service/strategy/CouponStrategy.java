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
        CouponDetails details = coupon.details();
        LocalDate expiry = null;
        if (details instanceof CartWiseDetails d) expiry = d.expiryDate();
        else if (details instanceof ProductWiseDetails d) expiry = d.expiryDate();
        else if (details instanceof BxGyDetails d) expiry = d.expiryDate();

        return expiry != null && expiry.isBefore(LocalDate.now());
    }
}
