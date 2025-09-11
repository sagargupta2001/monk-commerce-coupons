package com.monk.commerce.entity;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum CouponType {
    CART_WISE("CART_WISE"),
    PRODUCT_WISE("PRODUCT_WISE"),
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
            if (ct.value.equalsIgnoreCase(type)
                    || type.replace("-", "_").equalsIgnoreCase(ct.name())) {
                return ct;
            }
        }
        throw new IllegalArgumentException("Invalid coupon type: " + type);
    }

}