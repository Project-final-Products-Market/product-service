package com.project_final.product_service.controller;

import com.project_final.product_service.model.Product;
import com.project_final.product_service.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotBlank;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Valid
@RestController
@RequestMapping("/api/products")
@CrossOrigin(origins = "*")
public class ProductController {

    @Autowired
    private ProductService productService;

    // Crear producto
    @PostMapping
    public ResponseEntity<Product> createProduct(@Valid @RequestBody Product product) {
        Product createdProduct = productService.createProduct(product);
        return new ResponseEntity<>(createdProduct, HttpStatus.CREATED);
    }

    // Obtener todos los productos
    @GetMapping
    public ResponseEntity<List<Product>> getAllProducts() {
        List<Product> products = productService.getAllProducts();
        return new ResponseEntity<>(products, HttpStatus.OK);
    }

    // Obtener producto por ID
    @GetMapping("/{id}")
    public ResponseEntity<Product> getProductById(@PathVariable Long id) {
        Optional<Product> product = productService.getProductById(id);
        return product.map(value -> new ResponseEntity<>(value, HttpStatus.OK))
                .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    // Actualizar producto
    @PutMapping("/{id}")
    public ResponseEntity<Product> updateProduct(@PathVariable Long id, @Valid @RequestBody Product productDetails) {
        Product updatedProduct = productService.updateProduct(id, productDetails);
        return new ResponseEntity<>(updatedProduct, HttpStatus.OK);
    }

    // Eliminar producto
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long id) {
        productService.deleteProduct(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    // Buscar productos por nombre
    @GetMapping("/search")
    public ResponseEntity<List<Product>> searchProducts(@RequestParam String name) {
        List<Product> products = productService.searchProductsByName(name);
        return new ResponseEntity<>(products, HttpStatus.OK);
    }

    // Obtener productos disponibles
    @GetMapping("/available")
    public ResponseEntity<List<Product>> getAvailableProducts() {
        List<Product> products = productService.getAvailableProducts();
        return new ResponseEntity<>(products, HttpStatus.OK);
    }

    // Buscar productos por rango de precio
    @GetMapping("/price-range")
    public ResponseEntity<List<Product>> getProductsByPriceRange(
            @RequestParam BigDecimal minPrice,
            @RequestParam BigDecimal maxPrice) {
        List<Product> products = productService.getProductsByPriceRange(minPrice, maxPrice);
        return new ResponseEntity<>(products, HttpStatus.OK);
    }

    // Obtener productos con stock bajo
    @GetMapping("/low-stock")
    public ResponseEntity<List<Product>> getLowStockProducts(
            @RequestParam(defaultValue = "10") Integer threshold) {
        List<Product> products = productService.getLowStockProducts(threshold);
        return new ResponseEntity<>(products, HttpStatus.OK);
    }

    // Reducir stock (endpoint interno para Order Service)
    @PutMapping("/{id}/reduce-stock")
    public ResponseEntity<Boolean> reduceStock(@PathVariable Long id, @RequestParam Integer quantity) {
        boolean success = productService.reduceStock(id, quantity);
        return new ResponseEntity<>(success, HttpStatus.OK);
    }

    // Aumentar stock (endpoint interno para cancelaciones)
    @PutMapping("/{id}/increase-stock")
    public ResponseEntity<Boolean> increaseStock(@PathVariable Long id, @RequestParam Integer quantity) {
        boolean success = productService.increaseStock(id, quantity);
        return new ResponseEntity<>(success, HttpStatus.OK);
    }

    // Verificar stock disponible
    @GetMapping("/{id}/check-stock")
    public ResponseEntity<Boolean> checkStock(@PathVariable Long id, @RequestParam Integer quantity) {
        boolean hasStock = productService.hasEnoughStock(id, quantity);
        return new ResponseEntity<>(hasStock, HttpStatus.OK);
    }

    // Obtener estadísticas
    @GetMapping("/stats/total")
    public ResponseEntity<Long> getTotalProducts() {
        Long total = productService.getTotalProducts();
        return new ResponseEntity<>(total, HttpStatus.OK);
    }

    @GetMapping("/stats/available")
    public ResponseEntity<Long> getAvailableProductsCount() {
        Long count = productService.getAvailableProductsCount();
        return new ResponseEntity<>(count, HttpStatus.OK);
    }
}
