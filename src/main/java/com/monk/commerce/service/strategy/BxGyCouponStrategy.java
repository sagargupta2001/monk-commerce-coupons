package com.monk.commerce.service.strategy;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.monk.commerce.annotation.CouponHandler;
import com.monk.commerce.dto.*;
import com.monk.commerce.entity.CouponType;
import org.springframework.stereotype.Component;
import java.util.List;
import java.util.stream.Collectors;

@Component
@CouponHandler(CouponType.BXGY)
public class BxGyCouponStrategy implements CouponStrategy {

    private final ObjectMapper mapper = new ObjectMapper();

    private record BxGyDetails(
            List<Integer> buyProducts,
            List<Integer> getProducts,
            int buyQuantity,
            int getQuantity,
            int repetitionLimit
    ) {}

    @Override
    public CouponType getType() {
        return CouponType.BXGY;
    }

    @Override
    public boolean isApplicable(CartRequest cart, CouponResponse coupon) {
        var details = mapper.convertValue(coupon.details(), BxGyDetails.class);
        int buyCount = cart.items().stream()
                .filter(i -> details.buyProducts().contains(i.productId()))
                .mapToInt(CartItem::quantity).sum();
        int getCount = cart.items().stream()
                .filter(i -> details.getProducts().contains(i.productId()))
                .mapToInt(CartItem::quantity).sum();
        return buyCount >= details.buyQuantity() && getCount > 0;
    }

    @Override
    public double calculateDiscount(CartRequest cart, CouponResponse coupon) {
        var details = mapper.convertValue(coupon.details(), BxGyDetails.class);
        int buyCount = cart.items().stream()
                .filter(i -> details.buyProducts().contains(i.productId()))
                .mapToInt(CartItem::quantity).sum();
        int applicableTimes = Math.min(buyCount / details.buyQuantity(), details.repetitionLimit());
        List<CartItem> getItems = cart.items().stream()
                .filter(i -> details.getProducts().contains(i.productId()))
                .collect(Collectors.toList());

        double discount = 0;
        int freeItems = applicableTimes * details.getQuantity();

        for (CartItem gi : getItems) {
            int free = Math.min(freeItems, gi.quantity());
            discount += gi.price() * free;
            freeItems -= free;
            if (freeItems <= 0) break;
        }
        return discount;
    }

    @Override
    public ApplyCouponResponse applyCoupon(CartRequest cart, CouponResponse coupon) {
        var details = mapper.convertValue(coupon.details(), BxGyDetails.class);
        double totalPrice = cart.items().stream().mapToDouble(i -> i.price() * i.quantity()).sum();
        double totalDiscount = calculateDiscount(cart, coupon);
        List<DiscountedItem> items = cart.items().stream()
                .map(i -> new DiscountedItem(i.productId(), i.quantity(), i.price(), 0.0)) // distribute if needed
                .collect(Collectors.toList());
        return new ApplyCouponResponse(items, totalPrice, totalDiscount, totalPrice - totalDiscount);
    }
}