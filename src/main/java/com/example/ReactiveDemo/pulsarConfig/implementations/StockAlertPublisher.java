package com.example.ReactiveDemo.pulsarConfig.implementations;

import com.example.ReactiveDemo.pulsarConfig.alerts.LowStockAlertEvent;
import reactor.core.publisher.Mono;

public interface StockAlertPublisher {
    Mono<Void> publishAlert(LowStockAlertEvent alert);
}
