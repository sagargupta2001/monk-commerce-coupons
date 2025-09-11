package com.monk.commerce.entity;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CouponTypeTest {

    @Test
    void testFromString_MatchByValue() {
        // Matches the 'value' field
        assertEquals(CouponType.CART_WISE, CouponType.fromString("CART_WISE"));
        assertEquals(CouponType.PRODUCT_WISE, CouponType.fromString("PRODUCT_WISE"));
        assertEquals(CouponType.BXGY, CouponType.fromString("BXGY"));
    }

    @Test
    void testFromString_MatchByNameIgnoreCase() {
        // Matches the enum name directly (branch ct.name().equalsIgnoreCase(type))
        assertEquals(CouponType.CART_WISE, CouponType.fromString("CART_WISE")); // exact name
        assertEquals(CouponType.PRODUCT_WISE, CouponType.fromString("product_wise")); // different case
        assertEquals(CouponType.BXGY, CouponType.fromString("bxgy")); // lower case
    }

    @Test
    void testFromString_MatchByHyphenReplacement() {
        // Matches after replacing '-' with '_'
        assertEquals(CouponType.CART_WISE, CouponType.fromString("cart-wise"));
        assertEquals(CouponType.PRODUCT_WISE, CouponType.fromString("product-wise"));
    }

    @Test
    void testFromString_InvalidValue_ThrowsException() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                CouponType.fromString("INVALID")
        );
        assertEquals("Invalid coupon type: INVALID", exception.getMessage());
    }

    @Test
    void testFromString_MatchByNameBranch() {
        assertEquals(CouponType.CART_WISE, CouponType.fromString("CART_WISE"));
    }
}