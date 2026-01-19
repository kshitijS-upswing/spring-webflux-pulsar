package com.example.ReactiveDemo.controller;

import com.example.ReactiveDemo.controller.requests.CreateProductRequest;
import com.example.ReactiveDemo.controller.DTO.ProductDTO;
import com.example.ReactiveDemo.controller.implementations.ProductController;
import com.example.ReactiveDemo.controller.requests.UpdateStockRequest;
import com.example.ReactiveDemo.service.implementations.ProductService;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@RestController
@RequestMapping("/products")
@AllArgsConstructor
public class ProductControllerImpl implements ProductController {
    private final ProductService productService;


    // CREATE PRODUCT
    // POST /products (body)
    @Override
    @PostMapping
    public Mono<ProductDTO> createProduct(@RequestBody CreateProductRequest request) {
        return productService.createProduct(request.getProdName(), request.getProdCost(), request.getQuantity());
    }

    // CREATE PRODUCT
    // GET /products/...
    @Override
    @GetMapping("/{prodId}")
    public Mono<ProductDTO> getProductById(@PathVariable UUID prodId) {
        return productService.findProductById(prodId);
    }

    // CREATE PRODUCT
    // GET /products
    @Override
    @GetMapping
    public Flux<ProductDTO> getProducts() {
        return productService.getAllProducts();
    }


    // CREATE PRODUCT
    // PATCH /products (body)
    @Override
    @PatchMapping
    public Mono<ProductDTO> updateProductQuantity(@RequestBody UpdateStockRequest request) {
        return productService.updateStock(request.getProdId(), request.getDelta());
    }
}
