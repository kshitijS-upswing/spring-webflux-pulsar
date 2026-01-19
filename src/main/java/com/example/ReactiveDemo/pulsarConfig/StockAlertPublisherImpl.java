package com.example.ReactiveDemo.pulsarConfig;

import com.example.ReactiveDemo.pulsarConfig.alerts.LowStockAlertEvent;
import com.example.ReactiveDemo.pulsarConfig.implementations.StockAlertPublisher;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.pulsar.reactive.core.ReactivePulsarTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@Slf4j
@AllArgsConstructor
public class StockAlertPublisherImpl implements StockAlertPublisher {
    private final ReactivePulsarTemplate<LowStockAlertEvent> pulsarTemplate;

    @Override
    public Mono<Void> publishAlert(LowStockAlertEvent alert) {
        return pulsarTemplate.send("low-stock-alert", alert).then();
    }
}
