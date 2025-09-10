package com.monk.commerce.service.strategy;

import com.monk.commerce.entity.CouponType;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class CouponStrategyFactory {
    private final Map<CouponType, CouponStrategy> strategies = new HashMap<>();

    public CouponStrategyFactory(List<CouponStrategy> strategyList) {
        for (CouponStrategy strategy : strategyList) {
            strategies.put(strategy.getType(), strategy);
        }
    }

    public CouponStrategy getStrategy(CouponType type) {
        return strategies.get(type);
    }
}