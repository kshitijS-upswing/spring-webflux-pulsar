package com.example.ReactiveDemo.pulsarConfig;

import com.example.ReactiveDemo.pulsarConfig.alerts.LowStockAlertEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.pulsar.annotation.PulsarListener;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class StockAlertSubscriberImpl {

    @PulsarListener(
            subscriptionName = "low-stock-alert-sub",
            topics = "low-stock-alert"
    )
    public void handle(LowStockAlertEvent event) {
        log.warn(
                "🚨 LOW STOCK ALERT: product={}, remaining={}",
                event.getProductId(),
                event.getQuantity()
        );

    }
}
