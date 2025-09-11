package com.monk.commerce.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public class CouponExpiredException extends ResponseStatusException {
    public CouponExpiredException(Integer couponId) {
        super(HttpStatus.BAD_REQUEST, "Coupon " + couponId + " has expired");
    }
}
