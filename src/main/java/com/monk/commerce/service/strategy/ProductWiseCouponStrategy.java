package com.monk.commerce.service.strategy;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.monk.commerce.annotation.CouponHandler;
import com.monk.commerce.dto.*;
import com.monk.commerce.entity.CouponType;
import com.monk.commerce.exception.CouponExpiredException;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@CouponHandler(CouponType.PRODUCT_WISE)
public class ProductWiseCouponStrategy implements CouponStrategy {
    private final ObjectMapper mapper;

    public ProductWiseCouponStrategy(ObjectMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public CouponType getType() {
        return CouponType.PRODUCT_WISE;
    }

    @Override
    public boolean isApplicable(CartRequest cart, CouponResponse coupon) {
        if (isExpired(coupon)) return false;
        var details = mapper.convertValue(coupon.details(), ProductWiseDetails.class);
        return cart.items().stream().anyMatch(i -> i.productId().equals(details.productId()));
    }

    @Override
    public double calculateDiscount(CartRequest cart, CouponResponse coupon) {
        var details = mapper.convertValue(coupon.details(), ProductWiseDetails.class);
        return cart.items().stream()
                .filter(i -> i.productId().equals(details.productId()))
                .mapToDouble(i -> i.price() * i.quantity() * details.discount() / 100)
                .sum();
    }

    @Override
    public ApplyCouponResponse applyCoupon(CartRequest cart, CouponResponse coupon) {
        if (isExpired(coupon)) throw new CouponExpiredException(coupon.id());;
        var details = mapper.convertValue(coupon.details(), ProductWiseDetails.class);

        List<DiscountedItem> discountedItems = cart.items().stream()
                .map(i -> {
                    double itemDiscount = 0;
                    if (i.productId().equals(details.productId())) {
                        itemDiscount = i.price() * i.quantity() * details.discount() / 100;
                    }
                    return new DiscountedItem(i.productId(), i.quantity(), i.price(), itemDiscount);
                })
                .toList();

        double totalPrice = cart.items().stream()
                .mapToDouble(i -> i.price() * i.quantity())
                .sum();

        double totalDiscount = discountedItems.stream()
                .mapToDouble(DiscountedItem::totalDiscount)
                .sum();

        return new ApplyCouponResponse(discountedItems, totalPrice, totalDiscount, totalPrice - totalDiscount);
    }
}