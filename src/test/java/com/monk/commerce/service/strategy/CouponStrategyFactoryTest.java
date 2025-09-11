package com.monk.commerce.service.strategy;

import com.monk.commerce.annotation.CouponHandler;
import com.monk.commerce.dto.ApplyCouponResponse;
import com.monk.commerce.dto.CartRequest;
import com.monk.commerce.dto.CouponResponse;
import com.monk.commerce.entity.CouponType;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CouponStrategyFactoryTest {

    @CouponHandler(CouponType.CART_WISE)
    static class AnnotatedStrategy implements CouponStrategy {
        @Override public boolean isApplicable(CartRequest cart, CouponResponse coupon) { return false; }
        @Override public double calculateDiscount(CartRequest cart, CouponResponse coupon) { return 0; }
        @Override public ApplyCouponResponse applyCoupon(CartRequest cart, CouponResponse coupon) { return null; }
        @Override public CouponType getType() { return CouponType.CART_WISE; }
    }

    static class NonAnnotatedStrategy implements CouponStrategy {
        @Override public boolean isApplicable(CartRequest cart, CouponResponse coupon) { return false; }
        @Override public double calculateDiscount(CartRequest cart, CouponResponse coupon) { return 0; }
        @Override public ApplyCouponResponse applyCoupon(CartRequest cart, CouponResponse coupon) { return null; }
        @Override public CouponType getType() { return CouponType.CART_WISE; }
    }

    @Test
    void testAnnotatedStrategy() {
        CouponStrategyFactory factory = new CouponStrategyFactory(List.of(new AnnotatedStrategy()));
        CouponStrategy retrieved = factory.getStrategy(CouponType.CART_WISE);
        assertNotNull(retrieved);
        assertTrue(retrieved instanceof AnnotatedStrategy);
    }

    @Test
    void testNonAnnotatedStrategy() {
        // This should not throw and the map should remain empty
        CouponStrategyFactory factory = new CouponStrategyFactory(List.of(new NonAnnotatedStrategy()));
        assertNull(factory.getStrategy(CouponType.CART_WISE));
    }
}