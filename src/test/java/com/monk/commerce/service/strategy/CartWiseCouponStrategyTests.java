package com.monk.commerce.service.strategy;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.monk.commerce.dto.*;
import com.monk.commerce.entity.CouponType;
import com.monk.commerce.exception.CouponExpiredException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CartWiseCouponStrategyTests {

    private ObjectMapper mapper;
    private CartWiseCouponStrategy strategy;

    @BeforeEach
    void setUp() {
        mapper = spy(new ObjectMapper());
        mapper.registerModule(new JavaTimeModule());
        strategy = new CartWiseCouponStrategy(mapper);
    }

    private CartRequest sampleCart() {
        return new CartRequest(List.of(
                new CartItem(1, 2, 50.0),
                new CartItem(2, 1, 30.0)
        ));
    }

    private CouponResponse sampleCoupon(double threshold, double discount, boolean expired) {
        CartWiseDetails details = new CartWiseDetails(threshold, discount, expired ? LocalDate.now().minusDays(1) : LocalDate.now().plusDays(1));
        return new CouponResponse(1, CouponType.CART_WISE, details);
    }

    @Test
    void testGetType() {
        assertEquals(CouponType.CART_WISE, strategy.getType());
    }

    @Test
    void testIsApplicable_Valid() {
        CouponResponse coupon = sampleCoupon(50, 10, false); // threshold 50, cart total = 130
        assertTrue(strategy.isApplicable(sampleCart(), coupon));
    }

    @Test
    void testIsApplicable_NotApplicable_LowTotal() {
        CouponResponse coupon = sampleCoupon(200, 10, false); // threshold 200, cart total = 130
        assertFalse(strategy.isApplicable(sampleCart(), coupon));
    }

    @Test
    void testIsApplicable_ExpiredCoupon() {
        CouponResponse coupon = sampleCoupon(50, 10, true); // expired
        assertFalse(strategy.isApplicable(sampleCart(), coupon));
    }

    @Test
    void testIsApplicable_CatchBlock() {
        CouponResponse coupon = sampleCoupon(50, 10, false);
        // Force mapper.convertValue to throw exception
        doThrow(new RuntimeException("fail")).when(mapper).convertValue(any(), eq(CartWiseDetails.class));

        assertFalse(strategy.isApplicable(sampleCart(), coupon));
    }

    @Test
    void testCalculateDiscount_Applicable() {
        CouponResponse coupon = sampleCoupon(100, 10, false); // cart total = 130
        double discount = strategy.calculateDiscount(sampleCart(), coupon);
        assertEquals(13.0, discount, 0.001); // 10% of 130
    }

    @Test
    void testCalculateDiscount_NotApplicable() {
        CouponResponse coupon = sampleCoupon(200, 10, false); // cart total = 130
        double discount = strategy.calculateDiscount(sampleCart(), coupon);
        assertEquals(0.0, discount, 0.001);
    }

    @Test
    void testApplyCoupon_Valid() {
        CouponResponse coupon = sampleCoupon(50, 10, false); // total = 130
        ApplyCouponResponse response = strategy.applyCoupon(sampleCart(), coupon);

        assertEquals(130.0, response.totalPrice(), 0.001);
        assertEquals(13.0, response.totalDiscount(), 0.001);
        assertEquals(117.0, response.finalPrice(), 0.001);
        assertEquals(2, response.items().size());
    }

    @Test
    void testApplyCoupon_Expired() {
        CouponResponse coupon = sampleCoupon(50, 10, true); // expired

        assertThrows(CouponExpiredException.class, () -> strategy.applyCoupon(sampleCart(), coupon));
    }
}