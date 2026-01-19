package com.example.ReactiveDemo.pulsarConfig.alerts;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class LowStockAlertEvent {
    private UUID productId;
    private String productName;
    private int quantity;
}
