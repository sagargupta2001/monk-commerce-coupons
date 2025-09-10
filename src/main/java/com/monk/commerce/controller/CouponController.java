package com.monk.commerce.controller;

import com.monk.commerce.dto.*;
import com.monk.commerce.service.CouponService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/coupons")
public class CouponController {

    private final CouponService service;

    public CouponController(CouponService service) {
        this.service = service;
    }

    @PostMapping
    public CouponResponse createCoupon(@RequestBody CouponRequest request) {
        return service.createCoupon(request);
    }

    @GetMapping
    public List<CouponResponse> getAllCoupons() {
        return service.getAllCoupons();
    }

    @GetMapping("/{id}")
    public CouponResponse getCoupon(@PathVariable Integer id) {
        return service.getCoupon(id);
    }

    @PutMapping("/{id}")
    public CouponResponse updateCoupon(@PathVariable Integer id, @RequestBody CouponRequest request) {
        return service.updateCoupon(id, request);
    }

    @DeleteMapping("/{id}")
    public void deleteCoupon(@PathVariable Integer id) {
        service.deleteCoupon(id);
    }

    @PostMapping("/applicable-coupons")
    public ApplicableCouponsResponse getApplicableCoupons(@RequestBody CartRequest cart) {
        return service.getApplicableCoupons(cart);
    }

    @PostMapping("/apply-coupon/{id}")
    public ApplyCouponResponse applyCoupon(@PathVariable Integer id, @RequestBody CartRequest cart) {
        return service.applyCoupon(id, cart);
    }
}
