package com.monk.commerce.service.strategy;

import com.monk.commerce.dto.*;
import com.monk.commerce.entity.CouponType;

public interface CouponStrategy {

    boolean isApplicable(CartRequest cart, CouponResponse coupon);

    double calculateDiscount(CartRequest cart, CouponResponse coupon);

    ApplyCouponResponse applyCoupon(CartRequest cart, CouponResponse coupon);

    CouponType getType();
}
