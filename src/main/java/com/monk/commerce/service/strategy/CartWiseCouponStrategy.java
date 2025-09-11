package com.monk.commerce.service.strategy;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.monk.commerce.annotation.CouponHandler;
import com.monk.commerce.dto.*;
import com.monk.commerce.entity.CouponType;
import com.monk.commerce.exception.CouponExpiredException;
import org.springframework.stereotype.Component;
import java.util.List;

@Component
@CouponHandler(CouponType.CART_WISE)
public class CartWiseCouponStrategy implements CouponStrategy {
    private final ObjectMapper mapper;

    public CartWiseCouponStrategy(ObjectMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public CouponType getType() {
        return CouponType.CART_WISE;
    }

    @Override
    public boolean isApplicable(CartRequest cart, CouponResponse coupon) {
        if (isExpired(coupon)) return false;
        try {
            var details = mapper.convertValue(coupon.details(), CartWiseDetails.class);
            double total = cart.items().stream().mapToDouble(i -> i.price() * i.quantity()).sum();
            return total >= details.threshold();
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public double calculateDiscount(CartRequest cart, CouponResponse coupon) {
        var details = mapper.convertValue(coupon.details(), CartWiseDetails.class);
        double total = cart.items().stream().mapToDouble(i -> i.price() * i.quantity()).sum();
        return total >= details.threshold() ? total * details.discount() / 100 : 0;
    }

    @Override
    public ApplyCouponResponse applyCoupon(CartRequest cart, CouponResponse coupon) {
        if (isExpired(coupon)) throw new CouponExpiredException(coupon.id());;
        double discount = calculateDiscount(cart, coupon);
        double total = cart.items().stream().mapToDouble(i -> i.price() * i.quantity()).sum();
        List<DiscountedItem> discountedItems = cart.items().stream().map(i ->
                new DiscountedItem(i.productId(), i.quantity(), i.price(), 0.0) // discount not tracked at item level
        ).toList();
        return new ApplyCouponResponse(discountedItems, total, discount, total - discount);
    }
}