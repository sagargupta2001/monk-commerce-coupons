package com.monk.commerce.annotation;

import com.monk.commerce.entity.CouponType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface CouponHandler {
    CouponType value();
}
