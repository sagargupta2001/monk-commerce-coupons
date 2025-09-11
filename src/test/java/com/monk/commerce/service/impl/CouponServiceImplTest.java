package com.monk.commerce.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.monk.commerce.dto.*;
import com.monk.commerce.entity.Coupon;
import com.monk.commerce.entity.CouponType;
import com.monk.commerce.exception.CouponNotFoundException;
import com.monk.commerce.repository.CouponRepository;
import com.monk.commerce.service.strategy.BxGyCouponStrategy;
import com.monk.commerce.service.strategy.CartWiseCouponStrategy;
import com.monk.commerce.service.strategy.CouponStrategyFactory;
import com.monk.commerce.service.strategy.ProductWiseCouponStrategy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class CouponServiceImplTest {

    private CouponRepository repository;
    private CouponServiceImpl service;
    private ObjectMapper mapper;


    @BeforeEach
    void setUp() {
        repository = mock(CouponRepository.class);

        mapper = spy(new ObjectMapper());
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        CouponStrategyFactory factory = new CouponStrategyFactory(List.of(
                new CartWiseCouponStrategy(mapper),
                new ProductWiseCouponStrategy(mapper),
                new BxGyCouponStrategy(mapper)
        ));

        service = new CouponServiceImpl(repository, factory, mapper);
    }


    private CartRequest sampleCart() {
        return new CartRequest(List.of(
                new CartItem(1, 6, 50.0), // Product X
                new CartItem(2, 3, 30.0), // Product Y
                new CartItem(3, 2, 25.0)  // Product Z
        ));
    }

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

    @Test
    void testBxGyCoupon() {
        Coupon coupon = new Coupon();
        coupon.setId(3);
        coupon.setType(CouponType.BXGY);
        coupon.setDetails("""
                    {
                      "buyProducts":[{"productId":1,"quantity":2},{"productId":2,"quantity":2}],
                      "getProducts":[{"productId":3,"quantity":1}],
                      "repetitionLimit":3
                    }
                """);

        when(repository.findById(3)).thenReturn(java.util.Optional.of(coupon));

        ApplyCouponResponse applied = service.applyCoupon(3, sampleCart());

        // Buy 6 from [1,2] => 3 times applicable
        // Get 1 product Z free per repetition => 2 free (since only 2 exist in cart)
        assertEquals(25.0, applied.totalDiscount(), 0.001);
        assertEquals(415.0, applied.finalPrice(), 0.001);
    }

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
                            {
                              "buyProducts":[{"productId":1,"quantity":2},{"productId":2,"quantity":2}],
                              "getProducts":[{"productId":3,"quantity":1}],
                              "repetitionLimit":3
                            }
                        """)
                .build();

        when(repository.findAll()).thenReturn(List.of(c1, c2, c3));

        ApplicableCouponsResponse applicable = service.getApplicableCoupons(sampleCart());
        assertEquals(3, applicable.applicableCoupons().size());
    }

    @Test
    void testBxGyNotApplicable() {
        Coupon couponEntity = Coupon.builder()
                .id(4)
                .type(CouponType.BXGY)
                .details("""
                            {
                              "buyProducts":[{"productId":1,"quantity":2},{"productId":2,"quantity":2}],
                              "getProducts":[{"productId":3,"quantity":1}],
                              "repetitionLimit":3
                            }
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

    @Test
    void testBxGyPartialRepetition() {
        Coupon couponEntity = Coupon.builder()
                .id(5)
                .type(CouponType.BXGY)
                .details("""
                            {
                              "buyProducts":[{"productId":1,"quantity":2}],
                              "getProducts":[{"productId":3,"quantity":1}],
                              "repetitionLimit":3
                            }
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

    @Test
    void testBxGyMultipleGetProducts() {
        Coupon couponEntity = Coupon.builder()
                .id(6)
                .type(CouponType.BXGY)
                .details("""
                            {
                              "buyProducts":[{"productId":1,"quantity":2}],
                              "getProducts":[
                                {"productId":3,"quantity":1},
                                {"productId":4,"quantity":1},
                                {"productId":5,"quantity":1}
                              ],
                              "repetitionLimit":3
                            }
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

    @Test
    void testCreateCoupon() {
        CouponRequest request = new CouponRequest(
                CouponType.CART_WISE,
                new CartWiseDetails(200.0, 15.0, null)
        );

        // Stub repository save
        when(repository.save(any(Coupon.class))).thenAnswer(inv -> {
            Coupon c = inv.getArgument(0);
            c.setId(1);
            return c;
        });

        CouponResponse response = service.createCoupon(request);

        assertNotNull(response);
        assertEquals(CouponType.CART_WISE, response.type());
        CartWiseDetails details = (CartWiseDetails) response.details();
        assertEquals(200.0, details.threshold());
        assertEquals(15.0, details.discount());
    }

    @Test
    void testUpdateCoupon() {
        Coupon existing = new Coupon();
        existing.setId(1);
        existing.setType(CouponType.CART_WISE);
        existing.setDetails("{\"threshold\":100,\"discount\":10}");

        when(repository.findById(1)).thenReturn(Optional.of(existing));
        when(repository.save(any(Coupon.class))).thenAnswer(inv -> inv.getArgument(0));

        CouponRequest updateRequest = new CouponRequest(
                CouponType.CART_WISE,
                new CartWiseDetails(300.0, 20.0, null)
        );

        CouponResponse updated = service.updateCoupon(1, updateRequest);

        assertNotNull(updated);
        CartWiseDetails details = (CartWiseDetails) updated.details();
        assertEquals(300.0, details.threshold());
        assertEquals(20.0, details.discount());
    }

    @Test
    void testDeleteCoupon() {
        doNothing().when(repository).deleteById(1);

        service.deleteCoupon(1);

        verify(repository, times(1)).deleteById(1);
    }

    @Test
    void testGetCouponNotFound() {
        when(repository.findById(99)).thenReturn(Optional.empty());

        assertThrows(CouponNotFoundException.class, () -> service.getCoupon(99));
    }

    @Test
    void testToResponseParsingFailure() {
        Coupon bad = new Coupon();
        bad.setId(42);
        bad.setType(CouponType.CART_WISE);
        bad.setDetails("invalid-json"); // invalid JSON triggers exception

        // repository not used here, directly call private logic through public API
        when(repository.findById(42)).thenReturn(Optional.of(bad));

        RuntimeException ex = assertThrows(RuntimeException.class, () -> service.getCoupon(42));
        assertTrue(ex.getMessage().contains("Failed to parse coupon details"));
    }

    @Test
    void testCreateCouponSerializationFailure() throws Exception {
        CouponRequest request = new CouponRequest(
                CouponType.CART_WISE,
                new CartWiseDetails(100.0, 10.0, null)
        );

        // make mapper throw exception when serializing
        doThrow(new JsonProcessingException("Serialization failed") {}).when(mapper).writeValueAsString(any());

        assertThrows(RuntimeException.class, () -> service.createCoupon(request));

        // repository.save should never be called
        verify(repository, never()).save(any());
    }

    // ----------------- updateCoupon catch block -----------------
    @Test
    void testUpdateCouponSerializationFailure() throws Exception {
        Coupon existing = new Coupon();
        existing.setId(1);
        existing.setType(CouponType.CART_WISE);
        existing.setDetails("{\"threshold\":50,\"discount\":5}");

        when(repository.findById(1)).thenReturn(Optional.of(existing));

        CouponRequest request = new CouponRequest(
                CouponType.CART_WISE,
                new CartWiseDetails(200.0, 15.0, null)
        );

        doThrow(new JsonProcessingException("Serialization failed") {}).when(mapper).writeValueAsString(any());

        assertThrows(RuntimeException.class, () -> service.updateCoupon(1, request));

        // repository.save should never be called
        verify(repository, never()).save(any());
    }

    @Test
    void testGetAllCouponsCoversMapping() {
        Coupon c1 = Coupon.builder()
                .id(10)
                .type(CouponType.CART_WISE)
                .details("{\"threshold\":200,\"discount\":20}")
                .build();

        when(repository.findAll()).thenReturn(List.of(c1));

        List<CouponResponse> responses = service.getAllCoupons();

        assertEquals(1, responses.size());
        assertEquals(CouponType.CART_WISE, responses.get(0).type());
        CartWiseDetails details = (CartWiseDetails) responses.get(0).details();
        assertEquals(200.0, details.threshold());
        assertEquals(20.0, details.discount());
    }

}