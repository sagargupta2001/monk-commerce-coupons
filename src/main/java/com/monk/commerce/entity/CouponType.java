package com.monk.commerce.entity;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum CouponType {
    CART_WISE("CART-WISE"),
    PRODUCT_WISE("PRODUCT-WISE"),
    BXGY("BXGY");

    private final String value;

    CouponType(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }

    @JsonCreator
    public static CouponType fromString(String type) {
        for (CouponType ct : CouponType.values()) {
            if (ct.value.equalsIgnoreCase(type) || ct.name().equalsIgnoreCase(type)) {
                return ct;
            }
        }
        throw new IllegalArgumentException("Invalid coupon type: " + type);
    }
}