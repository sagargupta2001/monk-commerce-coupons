package com.monk.commerce.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public class CouponNotFoundException extends ResponseStatusException {
    public CouponNotFoundException(Integer id) {
        super(HttpStatus.NOT_FOUND, "Coupon with ID " + id + " not found");
    }
}
