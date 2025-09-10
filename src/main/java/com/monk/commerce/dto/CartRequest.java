package com.monk.commerce.dto;

import java.util.List;

public record CartRequest(
        List<CartItem> items
) {}