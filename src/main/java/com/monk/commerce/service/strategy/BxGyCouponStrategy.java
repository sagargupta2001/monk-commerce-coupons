package com.monk.commerce.service.strategy;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.monk.commerce.annotation.CouponHandler;
import com.monk.commerce.dto.*;
import com.monk.commerce.entity.CouponType;
import com.monk.commerce.exception.CouponExpiredException;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

@Component
@CouponHandler(CouponType.BXGY)
public class BxGyCouponStrategy implements CouponStrategy {

    private final ObjectMapper mapper;

    public BxGyCouponStrategy(ObjectMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public CouponType getType() {
        return CouponType.BXGY;
    }

    @Override
    public boolean isApplicable(CartRequest cart, CouponResponse coupon) {
        if (isExpired(coupon)) return false;
        var details = mapper.convertValue(coupon.details(), BxGyDetails.class);

        // Check if all buyProducts conditions are met
        boolean hasAllBuys = details.buyProducts().stream().allMatch(req -> {
            int qtyInCart = cart.items().stream()
                    .filter(i -> Objects.equals(i.productId(), req.productId()))
                    .mapToInt(CartItem::quantity)
                    .sum();
            return qtyInCart >= req.quantity();
        });

        // Check if at least one getProduct exists in cart
        boolean hasGetProduct = cart.items().stream()
                .anyMatch(i -> details.getProducts().stream()
                        .anyMatch(gp -> Objects.equals(gp.productId(), i.productId())));

        return hasAllBuys && hasGetProduct;
    }

    @Override
    public double calculateDiscount(CartRequest cart, CouponResponse coupon) {
        var details = mapper.convertValue(coupon.details(), BxGyDetails.class);

        // Calculate how many times offer can apply (based on min ratio of buyProducts)
        int maxApplications = details.repetitionLimit();

        for (ProductQuantity req : details.buyProducts()) {
            int qtyInCart = cart.items().stream()
                    .filter(i -> Objects.equals(i.productId(), req.productId()))
                    .mapToInt(CartItem::quantity)
                    .sum();
            int possibleApplications = qtyInCart / req.quantity();
            maxApplications = Math.min(maxApplications, possibleApplications);
        }

        if (maxApplications <= 0) return 0.0;

        // Now calculate free items
        List<CartItem> getItems = cart.items().stream()
                .filter(i -> details.getProducts().stream()
                        .anyMatch(gp -> Objects.equals(gp.productId(), i.productId())))
                .collect(Collectors.toList());

        double discount = 0.0;
        int finalMaxApplications = maxApplications;
        int totalFreeItems = details.getProducts().stream()
                .mapToInt(gp -> gp.quantity() * finalMaxApplications)
                .sum();

        // Sort getItems by price ascending (maximize savings for customer)
        getItems.sort(Comparator.comparingDouble(CartItem::price));

        for (CartItem gi : getItems) {
            int free = Math.min(totalFreeItems, gi.quantity());
            discount += gi.price() * free;
            totalFreeItems -= free;
            if (totalFreeItems <= 0) break;
        }

        return discount;
    }

    @Override
    public ApplyCouponResponse applyCoupon(CartRequest cart, CouponResponse coupon) {
        if (isExpired(coupon)) throw new CouponExpiredException(coupon.id());;
        double totalPrice = cart.items().stream().mapToDouble(i -> i.price() * i.quantity()).sum();
        double totalDiscount = calculateDiscount(cart, coupon);

        List<DiscountedItem> items = cart.items().stream()
                .map(i -> new DiscountedItem(i.productId(), i.quantity(), i.price(), 0.0))
                .toList();

        return new ApplyCouponResponse(items, totalPrice, totalDiscount, totalPrice - totalDiscount);
    }
}