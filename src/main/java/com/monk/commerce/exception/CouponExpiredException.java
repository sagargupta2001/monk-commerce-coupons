package com.monk.commerce.exception;

public class CouponExpiredException extends RuntimeException {
    public CouponExpiredException(Integer couponId) {
        super("Coupon " + couponId + " has expired");
    }
}
