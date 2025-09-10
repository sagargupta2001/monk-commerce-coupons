package com.monk.commerce.service.strategy;

import com.monk.commerce.annotation.CouponHandler;
import com.monk.commerce.entity.CouponType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class CouponStrategyFactory {
    private final Map<CouponType, CouponStrategy> strategyMap = new HashMap<>();

    @Autowired
    public CouponStrategyFactory(List<CouponStrategy> strategies) {
        for (CouponStrategy strategy : strategies) {
            CouponHandler annotation = strategy.getClass().getAnnotation(CouponHandler.class);
            if (annotation != null)
                strategyMap.put(annotation.value(), strategy);
        }
    }

    public CouponStrategy getStrategy(CouponType type) {
        return strategyMap.get(type);
    }
}