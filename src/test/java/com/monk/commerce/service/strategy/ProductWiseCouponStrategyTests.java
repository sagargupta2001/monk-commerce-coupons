package com.monk.commerce.service.strategy;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.monk.commerce.dto.*;
import com.monk.commerce.entity.CouponType;
import com.monk.commerce.exception.CouponExpiredException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ProductWiseCouponStrategyTests {

    private ProductWiseCouponStrategy strategy;
    private ObjectMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS); // Optional, for ISO format
        strategy = new ProductWiseCouponStrategy(mapper);
    }

    @Test
    void testGetType() {
        assertEquals(CouponType.PRODUCT_WISE, strategy.getType());
    }

    @Test
    void testIsApplicable_Valid() {
        CartRequest cart = new CartRequest(List.of(
                new CartItem(1, 2, 100.0),
                new CartItem(2, 1, 50.0)
        ));

        ProductWiseDetails details = new ProductWiseDetails(1, 10.0, LocalDate.now().plusDays(1));
        CouponResponse coupon = new CouponResponse(1, CouponType.PRODUCT_WISE, details);

        assertTrue(strategy.isApplicable(cart, coupon));
    }

    @Test
    void testIsApplicable_InvalidProduct() {
        CartRequest cart = new CartRequest(List.of(
                new CartItem(2, 1, 50.0)
        ));

        ProductWiseDetails details = new ProductWiseDetails(1, 10.0, LocalDate.now().plusDays(1));
        CouponResponse coupon = new CouponResponse(1, CouponType.PRODUCT_WISE, details);

        assertFalse(strategy.isApplicable(cart, coupon));
    }

    @Test
    void testIsApplicable_Expired() {
        CartRequest cart = new CartRequest(List.of(
                new CartItem(1, 2, 100.0)
        ));

        ProductWiseDetails details = new ProductWiseDetails(1, 10.0, LocalDate.now().minusDays(1));
        CouponResponse coupon = new CouponResponse(1, CouponType.PRODUCT_WISE, details);

        assertFalse(strategy.isApplicable(cart, coupon));
    }

    @Test
    void testCalculateDiscount_Valid() {
        CartRequest cart = new CartRequest(List.of(
                new CartItem(1, 2, 100.0), // eligible
                new CartItem(2, 1, 50.0)   // not eligible
        ));

        ProductWiseDetails details = new ProductWiseDetails(1, 10.0, LocalDate.now().plusDays(1));
        CouponResponse coupon = new CouponResponse(1, CouponType.PRODUCT_WISE, details);

        double discount = strategy.calculateDiscount(cart, coupon);
        assertEquals(20.0, discount); // 2 * 100 * 10% = 20
    }

    @Test
    void testCalculateDiscount_NoEligibleProduct() {
        CartRequest cart = new CartRequest(List.of(
                new CartItem(2, 1, 50.0)
        ));

        ProductWiseDetails details = new ProductWiseDetails(1, 10.0, LocalDate.now().plusDays(1));
        CouponResponse coupon = new CouponResponse(1, CouponType.PRODUCT_WISE, details);

        double discount = strategy.calculateDiscount(cart, coupon);
        assertEquals(0.0, discount);
    }

    @Test
    void testApplyCoupon_Valid() {
        CartRequest cart = new CartRequest(List.of(
                new CartItem(1, 2, 100.0),
                new CartItem(2, 1, 50.0)
        ));

        ProductWiseDetails details = new ProductWiseDetails(1, 10.0, LocalDate.now().plusDays(1));
        CouponResponse coupon = new CouponResponse(1, CouponType.PRODUCT_WISE, details);

        ApplyCouponResponse response = strategy.applyCoupon(cart, coupon);

        assertEquals(250.0, response.totalPrice());
        assertEquals(20.0, response.totalDiscount());
        assertEquals(230.0, response.finalPrice());

        // Check discounted items individually
        DiscountedItem item1 = response.items().get(0);
        assertEquals(1, item1.productId());
        assertEquals(20.0, item1.totalDiscount());

        DiscountedItem item2 = response.items().get(1);
        assertEquals(2, item2.productId());
        assertEquals(0.0, item2.totalDiscount());
    }

    @Test
    void testApplyCoupon_Expired() {
        CartRequest cart = new CartRequest(List.of(
                new CartItem(1, 2, 100.0)
        ));

        ProductWiseDetails details = new ProductWiseDetails(1, 10.0, LocalDate.now().minusDays(1));
        CouponResponse coupon = new CouponResponse(1, CouponType.PRODUCT_WISE, details);

        assertThrows(CouponExpiredException.class, () -> strategy.applyCoupon(cart, coupon));
    }
}