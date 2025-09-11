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

class BxGyCouponStrategyTests {

    private ObjectMapper mapper;
    private BxGyCouponStrategy strategy;

    @BeforeEach
    void setUp() {
        mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS); // Optional, for ISO format
        strategy = new BxGyCouponStrategy(mapper);
    }

    @Test
    void testGetType() {
        assertEquals(CouponType.BXGY, strategy.getType());
    }

    @Test
    void testIsApplicable_Valid() {
        CartRequest cart = new CartRequest(List.of(
                new CartItem(1, 2, 100.0),
                new CartItem(2, 1, 50.0)
        ));

        BxGyDetails details = new BxGyDetails(
                List.of(new ProductQuantity(1, 2)),
                List.of(new ProductQuantity(2, 1)),
                1,
                LocalDate.now().plusDays(1)
        );

        CouponResponse coupon = new CouponResponse(1, CouponType.BXGY, details);

        assertTrue(strategy.isApplicable(cart, coupon));
    }

    @Test
    void testIsApplicable_Expired() {
        CartRequest cart = new CartRequest(List.of(
                new CartItem(1, 2, 100.0),
                new CartItem(2, 1, 50.0)
        ));

        BxGyDetails details = new BxGyDetails(
                List.of(new ProductQuantity(1, 2)),
                List.of(new ProductQuantity(2, 1)),
                1,
                LocalDate.now().minusDays(1)
        );

        CouponResponse coupon = new CouponResponse(1, CouponType.BXGY, details);

        assertFalse(strategy.isApplicable(cart, coupon));
    }

    @Test
    void testCalculateDiscount_Valid() {
        CartRequest cart = new CartRequest(List.of(
                new CartItem(1, 4, 100.0),
                new CartItem(2, 2, 50.0)
        ));

        BxGyDetails details = new BxGyDetails(
                List.of(new ProductQuantity(1, 2)),
                List.of(new ProductQuantity(2, 1)),
                2,
                LocalDate.now().plusDays(1)
        );

        CouponResponse coupon = new CouponResponse(1, CouponType.BXGY, details);

        double discount = strategy.calculateDiscount(cart, coupon);
        assertEquals(100.0, discount); // 2 free items of product 2
    }

    @Test
    void testApplyCoupon_Valid() {
        CartRequest cart = new CartRequest(List.of(
                new CartItem(1, 2, 100.0),
                new CartItem(2, 3, 50.0)
        ));

        BxGyDetails details = new BxGyDetails(
                List.of(new ProductQuantity(1, 2)),
                List.of(new ProductQuantity(2, 3)),
                1,
                LocalDate.now().plusDays(1)
        );

        CouponResponse coupon = new CouponResponse(1, CouponType.BXGY, details);

        var response = strategy.applyCoupon(cart, coupon);

        assertEquals(350.0, response.totalPrice());
        assertEquals(150.0, response.totalDiscount());
        assertEquals(200.0, response.finalPrice());
    }

    @Test
    void testApplyCoupon_ExpiredCoupon() {
        CartRequest cart = new CartRequest(List.of(
                new CartItem(1, 4, 100.0),
                new CartItem(2, 2, 50.0)
        ));

        BxGyDetails details = new BxGyDetails(
                List.of(new ProductQuantity(1, 2)),
                List.of(new ProductQuantity(2, 1)),
                2,
                LocalDate.now().minusDays(1)
        );

        CouponResponse coupon = new CouponResponse(1, CouponType.BXGY, details);

        assertThrows(CouponExpiredException.class, () -> strategy.applyCoupon(cart, coupon));
    }

    @Test
    void testIsApplicable_AllConditionsMet() {
        CartRequest cart = new CartRequest(List.of(
                new CartItem(1, 2, 100.0),  // enough buy quantity
                new CartItem(2, 1, 50.0)    // get product exists
        ));
        BxGyDetails details = new BxGyDetails(
                List.of(new ProductQuantity(1, 2)),
                List.of(new ProductQuantity(2, 1)),
                1,
                LocalDate.now().plusDays(1)
        );
        CouponResponse coupon = new CouponResponse(1, CouponType.BXGY, details);

        assertTrue(strategy.isApplicable(cart, coupon)); // covers true branch of both
    }

    @Test
    void testIsApplicable_NotEnoughBuyQuantity() {
        CartRequest cart = new CartRequest(List.of(
                new CartItem(1, 1, 100.0),  // not enough buy quantity
                new CartItem(2, 1, 50.0)
        ));
        BxGyDetails details = new BxGyDetails(
                List.of(new ProductQuantity(1, 2)),
                List.of(new ProductQuantity(2, 1)),
                1,
                LocalDate.now().plusDays(1)
        );
        CouponResponse coupon = new CouponResponse(1, CouponType.BXGY, details);

        assertFalse(strategy.isApplicable(cart, coupon)); // covers false branch of qtyInCart >= req.quantity()
    }

    @Test
    void testIsApplicable_NoGetProductInCart() {
        CartRequest cart = new CartRequest(List.of(
                new CartItem(1, 2, 100.0)   // buy quantity met
                // no get product
        ));
        BxGyDetails details = new BxGyDetails(
                List.of(new ProductQuantity(1, 2)),
                List.of(new ProductQuantity(2, 1)),  // get product not in cart
                1,
                LocalDate.now().plusDays(1)
        );
        CouponResponse coupon = new CouponResponse(1, CouponType.BXGY, details);

        assertFalse(strategy.isApplicable(cart, coupon)); // covers false branch of hasAllBuys && hasGetProduct
    }
}