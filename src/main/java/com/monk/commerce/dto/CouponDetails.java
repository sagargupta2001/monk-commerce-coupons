package com.monk.commerce.dto;

public sealed interface CouponDetails
        permits CartWiseDetails, ProductWiseDetails, BxGyDetails {
}