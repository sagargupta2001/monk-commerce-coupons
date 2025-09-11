package com.monk.commerce.dto;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.monk.commerce.entity.CouponType;

public record CouponRequest(
        CouponType type,
        @JsonTypeInfo(
                use = JsonTypeInfo.Id.NAME,
                include = JsonTypeInfo.As.EXTERNAL_PROPERTY,
                property = "type"
        )
        @JsonSubTypes({
                @JsonSubTypes.Type(value = CartWiseDetails.class, name = "CART_WISE"),
                @JsonSubTypes.Type(value = ProductWiseDetails.class, name = "PRODUCT_WISE"),
                @JsonSubTypes.Type(value = BxGyDetails.class, name = "BXGY")
        })
        CouponDetails details
) {}