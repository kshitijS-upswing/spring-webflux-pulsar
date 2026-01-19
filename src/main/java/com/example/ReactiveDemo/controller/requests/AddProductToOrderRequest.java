package com.example.ReactiveDemo.controller.requests;

import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

// Request object to add a product to a particular user's order based on ID match only
@Getter
@Setter
public class AddProductToOrderRequest {
    private UUID productId;
    private int quantity;
}
