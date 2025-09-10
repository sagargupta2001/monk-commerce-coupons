package com.monk.commerce.service;

import com.monk.commerce.dto.*;

import java.util.List;

public interface CouponService {
    CouponResponse createCoupon(CouponRequest request);
    List<CouponResponse> getAllCoupons();
    CouponResponse getCoupon(Integer id);
    CouponResponse updateCoupon(Integer id, CouponRequest request);
    void deleteCoupon(Integer id);
    ApplicableCouponsResponse getApplicableCoupons(CartRequest cart);
    ApplyCouponResponse applyCoupon(Integer id, CartRequest cart);
}
