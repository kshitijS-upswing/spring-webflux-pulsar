package com.example.ReactiveDemo.service;

import com.example.ReactiveDemo.controller.DTO.OrderDTO;
import com.example.ReactiveDemo.controller.DTO.ProductOrderDTO;
import com.example.ReactiveDemo.pulsarConfig.alerts.LowStockAlertEvent;
import com.example.ReactiveDemo.pulsarConfig.implementations.StockAlertPublisher;
import com.example.ReactiveDemo.repository.OrderItemsRepository;
import com.example.ReactiveDemo.repository.OrderRepository;
import com.example.ReactiveDemo.repository.ProductRepository;
import com.example.ReactiveDemo.repository.UserRepository;
import com.example.ReactiveDemo.repository.entities.OrderEntity;
import com.example.ReactiveDemo.repository.entities.OrderItemsEntity;
import com.example.ReactiveDemo.repository.entities.OrderStatus;
import com.example.ReactiveDemo.service.implementations.OrderService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.UUID;

@Service
@AllArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepo;
    private final OrderItemsRepository orderItemsRepo;
    private final ProductRepository productRepo;
    private final StockAlertPublisher stockAlertPublisher;


    // Simple function to create orders for a particular user
    @Override
    public Mono<OrderDTO> createOrder(UUID userId) {
        OrderEntity entity = OrderEntity.builder()
                .userId(userId)
                .statusCode(OrderStatus.IN_PROGRESS.getCode())
                .build();

        return orderRepo.save(entity)
                .map(order -> OrderDTO.builder()
                        .orderId(order.getId())
                        .userId(order.getUserId())
                        .status(order.getStatus())
                        .products(List.of())
                        .build());
    }

    // Self explanatory name, get all orders placed by a user
    @Override
    public Flux<OrderDTO> getOrdersByUserId(UUID userId) {
        return orderRepo.findByUserId(userId)
                .flatMap(this::toDTOWithProducts);
    }

    // Self explanatory name, get all orders of a specific user by orderId
    @Override
    public Mono<OrderDTO> getOrderByOrderId(UUID userId, UUID orderId) {
        return orderRepo.findById(orderId)
                .filter(order -> order.getUserId().equals(userId))
                .switchIfEmpty(Mono.error(new IllegalArgumentException("Order not found")))
                .flatMap(this::toDTOWithProducts);
    }

    // Difficult to implement correctly
    // Function purpose is to finalize an order iff all products in "basket"
    // are less than available quantity
    @Override
    public Mono<OrderDTO> finalizeOrder(UUID userId, UUID orderId) {
        return orderRepo.findById(orderId)
                .filter(order -> order.getUserId().equals(userId))
                .switchIfEmpty(Mono.error(new IllegalStateException("Order not found")))
                .flatMap(order -> {

                    if (order.getStatus() != OrderStatus.IN_PROGRESS) {
                        return Mono.error(new IllegalStateException("Order already finalized"));
                    }

                    return orderItemsRepo.findByOrderId(order.getId())
                            .flatMap(this::validateAndReduceStock)
                            .flatMap(stockAlertPublisher::publishAlert)
                            .then(Mono.defer(() -> {
                                order.setStatus(OrderStatus.CONFIRMED);
                                return orderRepo.save(order);
                            }))
                            .flatMap(this::toDTOWithProducts);
                });
    }


    // Helper function to validate a product and reduce its stock in the DB
    private Mono<LowStockAlertEvent> validateAndReduceStock(OrderItemsEntity item) {
        return productRepo.findById(item.getProductId())
                .switchIfEmpty(Mono.error(new IllegalStateException("Product not found")))
                .flatMap(product -> {

                    if (product.getAvailableQuantity() < item.getQuantity()) {
                        return Mono.error(
                                new IllegalStateException(
                                        "Insufficient stock for product " + product.getId()
                                )
                        );
                    }

                    int remaining = product.getAvailableQuantity() - item.getQuantity();
                    product.setAvailableQuantity(remaining);

                    return productRepo.save(product).then(
                            Mono.justOrEmpty(
                                    remaining <= 5 ? new LowStockAlertEvent(
                                            product.getId(),
                                            product.getName(),
                                            remaining
                                    ) : null
                            )
                    );
                });
    }

    // Mapping function to convert an entity to DTO
    private Mono<OrderDTO> toDTOWithProducts(OrderEntity order) {
        return orderItemsRepo.findByOrderId(order.getId())
                .flatMap(item ->
                        productRepo.findById(item.getProductId())
                                .map(product -> ProductOrderDTO.builder()
                                        .productId(product.getId())
                                        .productName(product.getName())
                                        .productPrice(item.getPriceAtOrderTime())
                                        .productOrderQuantity(item.getQuantity())
                                        .build())
                )
                .collectList()
                .map(products -> OrderDTO.builder()
                        .orderId(order.getId())
                        .userId(order.getUserId())
                        .status(order.getStatus())
                        .products(products)
                        .build());
    }

    // Function to add a product to a user's basket for a particular order
    @Override
    public Mono<OrderDTO> addProductToOrder(
            UUID userId,
            UUID orderId,
            UUID productId,
            int quantity
    ) {
        return orderRepo.findById(orderId)
                .filter(order -> order.getUserId().equals(userId))
                .switchIfEmpty(Mono.error(new IllegalStateException("Order not found")))
                .flatMap(order -> {
                    if (order.getStatus() != OrderStatus.IN_PROGRESS) {
                        return Mono.error(new IllegalStateException("Cannot modify finalized order"));
                    }

                    return productRepo.findById(productId)
                            .switchIfEmpty(Mono.error(new IllegalStateException("Product not found")))
                            .flatMap(product -> {

                                OrderItemsEntity item = OrderItemsEntity.builder()
                                        .orderId(order.getId())
                                        .productId(product.getId())
                                        .quantity(quantity)
                                        .priceAtOrderTime(product.getPrice())
                                        .build();

                                return orderItemsRepo.save(item);
                            })
                            .then(toDTOWithProducts(order));
                });
    }

}
