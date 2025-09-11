package com.monk.commerce.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.monk.commerce.dto.*;
import com.monk.commerce.entity.CouponType;
import com.monk.commerce.service.CouponService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CouponController.class)
@Import(CouponControllerTests.MockConfig.class)
class CouponControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper mapper;

    // 👇 Inject the mock bean from MockConfig
    @Autowired
    private CouponService service;

    @TestConfiguration
    static class MockConfig {
        @Bean
        @Primary
        CouponService couponService() {
            return Mockito.mock(CouponService.class);
        }
    }

    private CouponResponse sampleResponse;

    @BeforeEach
    void setUp() {
        sampleResponse = new CouponResponse(
                1,
                CouponType.CART_WISE,
                new CartWiseDetails(100.0, 10.0, null)
        );
    }

    @Test
    void testCreateCoupon() throws Exception {
        CouponRequest request = new CouponRequest(
                CouponType.CART_WISE,
                new CartWiseDetails(100.0, 10.0, null)
        );
        Mockito.when(service.createCoupon(any())).thenReturn(sampleResponse);

        mockMvc.perform(post("/coupons")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.type").value("CART_WISE"));
    }

    @Test
    void testGetAllCoupons() throws Exception {
        Mockito.when(service.getAllCoupons()).thenReturn(List.of(sampleResponse));

        mockMvc.perform(get("/coupons"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].type").value("CART_WISE"));
    }

    @Test
    void testGetCouponById() throws Exception {
        Mockito.when(service.getCoupon(1)).thenReturn(sampleResponse);

        mockMvc.perform(get("/coupons/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void testUpdateCoupon() throws Exception {
        CouponRequest request = new CouponRequest(
                CouponType.CART_WISE,
                new CartWiseDetails(200.0, 15.0, null)
        );
        CouponResponse updatedResponse = new CouponResponse(
                1,
                CouponType.CART_WISE,
                new CartWiseDetails(200.0, 15.0, null)
        );

        Mockito.when(service.updateCoupon(eq(1), any())).thenReturn(updatedResponse);

        mockMvc.perform(put("/coupons/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.details.threshold").value(200.0))
                .andExpect(jsonPath("$.details.discount").value(15.0));
    }

    @Test
    void testDeleteCoupon() throws Exception {
        mockMvc.perform(delete("/coupons/1"))
                .andExpect(status().isOk());

        Mockito.verify(service).deleteCoupon(1);
    }

    @Test
    void testGetApplicableCoupons() throws Exception {
        CartRequest cart = new CartRequest(List.of(new CartItem(1, 2, 50.0)));
        ApplicableCouponsResponse applicable = new ApplicableCouponsResponse(
                List.of(new ApplicableCoupon(1, CouponType.CART_WISE, 10.0))
        );

        Mockito.when(service.getApplicableCoupons(any())).thenReturn(applicable);

        mockMvc.perform(post("/coupons/applicable-coupons")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(cart)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.applicableCoupons[0].couponId").value(1))
                .andExpect(jsonPath("$.applicableCoupons[0].discount").value(10.0))
                .andExpect(jsonPath("$.applicableCoupons[0].type").value("CART_WISE"));
    }

    @Test
    void testApplyCoupon() throws Exception {
        CartRequest cart = new CartRequest(List.of(new CartItem(1, 2, 50.0)));
        ApplyCouponResponse applied = new ApplyCouponResponse(
                List.of(new DiscountedItem(1, 2, 50.0, 0.0)),
                100.0, 10.0, 90.0
        );

        Mockito.when(service.applyCoupon(eq(1), any())).thenReturn(applied);

        mockMvc.perform(post("/coupons/apply-coupon/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(cart)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalPrice").value(100.0))
                .andExpect(jsonPath("$.totalDiscount").value(10.0))
                .andExpect(jsonPath("$.finalPrice").value(90.0));
    }
}