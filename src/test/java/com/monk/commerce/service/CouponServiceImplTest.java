package com.monk.commerce.service;

import com.monk.commerce.dto.ApplicableCouponsResponse;
import com.monk.commerce.dto.ApplyCouponResponse;
import com.monk.commerce.dto.CartItem;
import com.monk.commerce.dto.CartRequest;
import com.monk.commerce.entity.Coupon;
import com.monk.commerce.entity.CouponType;
import com.monk.commerce.repository.CouponRepository;
import com.monk.commerce.service.impl.CouponServiceImpl;
import com.monk.commerce.service.strategy.BxGyCouponStrategy;
import com.monk.commerce.service.strategy.CartWiseCouponStrategy;
import com.monk.commerce.service.strategy.CouponStrategyFactory;
import com.monk.commerce.service.strategy.ProductWiseCouponStrategy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class CouponServiceImplTest {

    private CouponRepository repository;
    private CouponServiceImpl service;

    @BeforeEach
    void setUp() {
        repository = mock(CouponRepository.class);
        CouponStrategyFactory factory = new CouponStrategyFactory(List.of(
                new CartWiseCouponStrategy(),
                new ProductWiseCouponStrategy(),
                new BxGyCouponStrategy()
        ));
        service = new CouponServiceImpl(repository, factory);
    }

    private CartRequest sampleCart() {
        return new CartRequest(List.of(
                new CartItem(1, 6, 50.0), // Product X
                new CartItem(2, 3, 30.0), // Product Y
                new CartItem(3, 2, 25.0)  // Product Z
        ));
    }

    // ---------- Cart-wise coupon ----------
    @Test
    void testCartWiseCoupon() {
        Coupon couponEntity = new Coupon();
        couponEntity.setId(1);
        couponEntity.setType(CouponType.CART_WISE);
        couponEntity.setDetails("{\"threshold\":100,\"discount\":10}");

        when(repository.findAll()).thenReturn(List.of(couponEntity));
        when(repository.findById(1)).thenReturn(java.util.Optional.of(couponEntity));

        ApplicableCouponsResponse applicable = service.getApplicableCoupons(sampleCart());
        assertEquals(1, applicable.applicableCoupons().size());
        assertEquals(44.0, applicable.applicableCoupons().get(0).discount(), 0.001);

        ApplyCouponResponse applied = service.applyCoupon(1, sampleCart());
        assertEquals(440.0, applied.totalPrice(), 0.001);
        assertEquals(44.0, applied.totalDiscount(), 0.001);
        assertEquals(396.0, applied.finalPrice(), 0.001);
    }

    // ---------- Product-wise coupon ----------
    @Test
    void testProductWiseCoupon() {
        Coupon coupon = new Coupon();
        coupon.setId(2);
        coupon.setType(CouponType.PRODUCT_WISE);
        coupon.setDetails("{\"productId\":1,\"discount\":20}");

        when(repository.findById(2)).thenReturn(java.util.Optional.of(coupon));

        ApplyCouponResponse applied = service.applyCoupon(2, sampleCart());
        // Product 1: 6 x 50 = 300, 20% discount = 60
        assertEquals(440.0, applied.totalPrice(), 0.001);
        assertEquals(60.0, applied.totalDiscount(), 0.001);
        assertEquals(380.0, applied.finalPrice(), 0.001);
    }

    // ---------- BxGy coupon ----------
    @Test
    void testBxGyCoupon() {
        Coupon coupon = new Coupon();
        coupon.setId(3);
        coupon.setType(CouponType.BXGY);
        coupon.setDetails("""
            {"buyProducts":[{"productId":1},{"productId":2}],
             "getProducts":[{"productId":3}],
             "buyQuantity":2,"getQuantity":1,"repetitionLimit":3}
        """);

        when(repository.findById(3)).thenReturn(java.util.Optional.of(coupon));

        ApplyCouponResponse applied = service.applyCoupon(3, sampleCart());

        // Buy 6 from [1,2] => 3 times applicable
        // Get 1 product Z free per repetition => 2 free (since only 2 exist in cart)
        assertEquals(50.0, applied.totalDiscount(), 0.001);
        assertEquals(390.0, applied.finalPrice(), 0.001);
    }

    // ---------- Applicable Coupons ----------
    @Test
    void testApplicableCoupons() {
        Coupon c1 = Coupon.builder()
                .id(1)
                .type(CouponType.CART_WISE)
                .details("{\"threshold\":100,\"discount\":10}")
                .build();

        Coupon c2 = Coupon.builder()
                .id(2)
                .type(CouponType.PRODUCT_WISE)
                .details("{\"productId\":1,\"discount\":20}")
                .build();

        Coupon c3 = Coupon.builder()
                .id(3)
                .type(CouponType.BXGY)
                .details("""
                    {"buyProducts":[{"productId":1},{"productId":2}],
                     "getProducts":[{"productId":3}],
                     "buyQuantity":2,"getQuantity":1,"repetitionLimit":3}
                """)
                .build();

        when(repository.findAll()).thenReturn(List.of(c1, c2, c3));

        ApplicableCouponsResponse applicable = service.getApplicableCoupons(sampleCart());
        assertEquals(3, applicable.applicableCoupons().size());
    }

    // ---------- BxGy Not Applicable Coupons ----------
    @Test
    void testBxGyNotApplicable() {
        Coupon couponEntity = Coupon.builder()
                .id(4)
                .type(CouponType.BXGY)
                .details("""
                    {"buyProducts":[{"productId":1},{"productId":2}],
                     "getProducts":[{"productId":3}],
                     "buyQuantity":2,"getQuantity":1,"repetitionLimit":3}
                """)
                .build();

        when(repository.findById(4)).thenReturn(java.util.Optional.of(couponEntity));

        // Cart with only 1 buy-product
        CartRequest cart = new CartRequest(List.of(
                new CartItem(1, 1, 50.0),
                new CartItem(3, 1, 25.0)
        ));

        ApplyCouponResponse applied = service.applyCoupon(4, cart);

        assertEquals(0.0, applied.totalDiscount(), 0.001);
        assertEquals(75.0, applied.finalPrice(), 0.001);
    }

    // ---------- BxGy Partial Repetition ----------
    @Test
    void testBxGyPartialRepetition() {
        Coupon couponEntity = Coupon.builder()
                .id(5)
                .type(CouponType.BXGY)
                .details("""
                    {"buyProducts":[{"productId":1},{"productId":2}],
                     "getProducts":[{"productId":3}],
                     "buyQuantity":2,"getQuantity":1,"repetitionLimit":3}
                """)
                .build();

        when(repository.findById(5)).thenReturn(java.util.Optional.of(couponEntity));

        CartRequest cart = new CartRequest(List.of(
                new CartItem(1, 5, 50.0),
                new CartItem(3, 2, 25.0)
        ));

        ApplyCouponResponse applied = service.applyCoupon(5, cart);

        // Max repetition = 2 (5/2 = 2), only 2 freebies available
        assertEquals(50.0, applied.totalDiscount(), 0.001);
        assertEquals(250.0, applied.finalPrice(), 0.001);
    }

    // ---------- BxGy Multiple Get-products ----------
    @Test
    void testBxGyMultipleGetProducts() {
        Coupon couponEntity = Coupon.builder()
                .id(6)
                .type(CouponType.BXGY)
                .details("""
                    {"buyProducts":[{"productId":1},{"productId":2}],
                     "getProducts":[{"productId":3},{"productId":4},{"productId":5}],
                     "buyQuantity":2,"getQuantity":1,"repetitionLimit":3}
                """)
                .build();

        when(repository.findById(6)).thenReturn(java.util.Optional.of(couponEntity));

        CartRequest cart = new CartRequest(List.of(
                new CartItem(1, 4, 50.0),
                new CartItem(3, 1, 25.0),
                new CartItem(4, 1, 30.0)
        ));

        ApplyCouponResponse applied = service.applyCoupon(6, cart);

        // 2 repetitions possible, 2 cheapest freebies applied (25 + 30)
        assertEquals(55.0, applied.totalDiscount(), 0.001);
    }
}