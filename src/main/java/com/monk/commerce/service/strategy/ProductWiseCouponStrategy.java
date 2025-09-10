package com.monk.commerce.service.strategy;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.monk.commerce.dto.*;
import com.monk.commerce.entity.CouponType;
import org.springframework.stereotype.Component;
import java.util.List;

@Component
public class ProductWiseCouponStrategy implements CouponStrategy {
    private final ObjectMapper mapper = new ObjectMapper();
    private record ProductWiseDetails(Integer productId, double discount) {}

    @Override
    public CouponType getType() {
        return CouponType.PRODUCT_WISE;
    }

    @Override
    public boolean isApplicable(CartRequest cart, CouponResponse coupon) {
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
        var details = mapper.convertValue(coupon.details(), ProductWiseDetails.class);
        List<DiscountedItem> discountedItems = cart.items().stream()
                .map(i -> {
                    double itemDiscount = 0;
                    if (i.productId().equals(details.productId()))
                        itemDiscount = i.price() * i.quantity() * details.discount() / 100;
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