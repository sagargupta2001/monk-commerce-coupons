package com.monk.commerce.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.monk.commerce.dto.*;
import com.monk.commerce.exception.CouponNotFoundException;
import com.monk.commerce.entity.Coupon;
import com.monk.commerce.repository.CouponRepository;
import com.monk.commerce.service.CouponService;
import com.monk.commerce.service.strategy.CouponStrategy;
import com.monk.commerce.service.strategy.CouponStrategyFactory;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CouponServiceImpl implements CouponService {

    private final CouponRepository repository;
    private final CouponStrategyFactory factory;
    private final ObjectMapper mapper = new ObjectMapper();

    public CouponServiceImpl(CouponRepository repository, CouponStrategyFactory factory) {
        this.repository = repository;
        this.factory = factory;
    }

    @Override
    public CouponResponse createCoupon(CouponRequest request) {
        Coupon entity = new Coupon();
        entity.setType(request.type());
        try {
            entity.setDetails(mapper.writeValueAsString(request.details()));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        repository.save(entity);
        return toResponse(entity);
    }

    @Override
    public List<CouponResponse> getAllCoupons() {
        return repository.findAll().stream().map(this::toResponse).toList();
    }

    @Override
    public CouponResponse getCoupon(Integer id) {
        return toResponse(repository.findById(id).orElseThrow(() -> new CouponNotFoundException(id)));
    }

    @Override
    public CouponResponse updateCoupon(Integer id, CouponRequest request) {
        Coupon entity = repository.findById(id).orElseThrow(() -> new CouponNotFoundException(id));
        entity.setType(request.type());
        try {
            entity.setDetails(mapper.writeValueAsString(request.details()));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        repository.save(entity);
        return toResponse(entity);
    }

    @Override
    public void deleteCoupon(Integer id) {
        repository.deleteById(id);
    }

    @Override
    public ApplicableCouponsResponse getApplicableCoupons(CartRequest cart) {
        List<ApplicableCoupon> applicable = repository.findAll().stream().map(this::toResponse)
                .filter(c -> {
                    CouponStrategy strategy = factory.getStrategy(c.type());
                    return strategy.isApplicable(cart, c);
                })
                .map(c -> {
                    CouponStrategy strategy = factory.getStrategy(c.type());
                    double discount = strategy.calculateDiscount(cart, c);
                    return new ApplicableCoupon(c.id(), c.type(), discount);
                }).toList();
        return new ApplicableCouponsResponse(applicable);
    }

    @Override
    public ApplyCouponResponse applyCoupon(Integer id, CartRequest cart) {
        CouponResponse coupon = getCoupon(id);
        CouponStrategy strategy = factory.getStrategy(coupon.type());
        return strategy.applyCoupon(cart, coupon);
    }

    private CouponResponse toResponse(Coupon entity) {
        Object details;
        try {
            details = mapper.readValue(entity.getDetails(), Object.class);
        } catch (Exception e) {
            details = null;
        }
        return new CouponResponse(entity.getId(), entity.getType(), details);
    }
}