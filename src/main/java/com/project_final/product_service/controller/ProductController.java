package com.project_final.product_service.controller;

import com.project_final.product_service.model.Product;
import com.project_final.product_service.service.ProductService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/products")
@CrossOrigin(origins = "*")
public class ProductController {

    private static final Logger logger = LoggerFactory.getLogger(ProductController.class);

    @Autowired
    private ProductService productService;

    // Crear producto
    @PostMapping
    public ResponseEntity<Map<String, Object>> createProduct(@Valid @RequestBody Product product) {
        logger.info("Petición para crear producto: {}", product.getName());

        try {
            Product createdProduct = productService.createProduct(product);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Producto creado correctamente");
            response.put("product", createdProduct);
            response.put("productId", createdProduct.getId());
            response.put("name", createdProduct.getName());
            response.put("price", createdProduct.getPrice());
            response.put("stock", createdProduct.getStock());
            response.put("timestamp", LocalDateTime.now());

            logger.info("Producto creado exitosamente con ID: {}", createdProduct.getId());
            return new ResponseEntity<>(response, HttpStatus.CREATED);

        } catch (RuntimeException e) {
            logger.error("Error creando producto: {}", e.getMessage());

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Error al crear producto");
            errorResponse.put("error", e.getMessage());
            errorResponse.put("timestamp", LocalDateTime.now());

            return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
        }
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
    public ResponseEntity<Map<String, Object>> updateProduct(@PathVariable Long id, @Valid @RequestBody Product productDetails) {
        logger.info("Petición para actualizar producto: {}", id);

        try {
            Product updatedProduct = productService.updateProduct(id, productDetails);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Producto actualizado correctamente");
            response.put("product", updatedProduct);
            response.put("productId", updatedProduct.getId());
            response.put("name", updatedProduct.getName());
            response.put("price", updatedProduct.getPrice());
            response.put("stock", updatedProduct.getStock());
            response.put("timestamp", LocalDateTime.now());

            logger.info("Producto {} actualizado exitosamente", id);
            return new ResponseEntity<>(response, HttpStatus.OK);

        } catch (RuntimeException e) {
            logger.error("Error actualizando producto {}: {}", id, e.getMessage());

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Error al actualizar producto");
            errorResponse.put("productId", id);
            errorResponse.put("error", e.getMessage());
            errorResponse.put("timestamp", LocalDateTime.now());

            return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
        }
    }

    // Eliminar producto
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deleteProduct(@PathVariable Long id) {
        logger.info("Petición para eliminar producto: {}", id);

        try {
            // Obtener información del producto antes de eliminarlo
            Optional<Product> productToDelete = productService.getProductById(id);

            if (productToDelete.isEmpty()) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("message", "Producto no encontrado");
                errorResponse.put("productId", id);
                errorResponse.put("timestamp", LocalDateTime.now());
                return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
            }

            Product product = productToDelete.get();

            // Advertencia si tiene stock
            if (product.getStock() > 0) {
                logger.warn("Eliminando producto {} con stock disponible: {}", id, product.getStock());
            }

            productService.deleteProduct(id);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Producto eliminado correctamente");
            response.put("productId", id);
            response.put("deletedProduct", product.getName());
            response.put("price", product.getPrice());
            response.put("stockAtDeletion", product.getStock());
            response.put("timestamp", LocalDateTime.now());

            logger.info("Producto {} eliminado exitosamente", id);
            return new ResponseEntity<>(response, HttpStatus.OK);

        } catch (RuntimeException e) {
            logger.error("Error eliminando producto {}: {}", id, e.getMessage());

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Error al eliminar producto");
            errorResponse.put("productId", id);
            errorResponse.put("error", e.getMessage());
            errorResponse.put("timestamp", LocalDateTime.now());

            return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
        }
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
    public ResponseEntity<Map<String, Object>> reduceStock(@PathVariable Long id, @RequestParam Integer quantity) {
        logger.info("Petición para reducir stock del producto {}: cantidad {}", id, quantity);

        try {
            boolean success = productService.reduceStock(id, quantity);

            Map<String, Object> response = new HashMap<>();
            response.put("success", success);
            response.put("message", success ? "Stock reducido correctamente" : "No se pudo reducir el stock");
            response.put("productId", id);
            response.put("quantityReduced", quantity);
            response.put("timestamp", LocalDateTime.now());

            return new ResponseEntity<>(response, HttpStatus.OK);

        } catch (RuntimeException e) {
            logger.error("Error reduciendo stock del producto {}: {}", id, e.getMessage());

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Error al reducir stock");
            errorResponse.put("productId", id);
            errorResponse.put("error", e.getMessage());
            errorResponse.put("timestamp", LocalDateTime.now());

            return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
        }
    }

    // Aumentar stock (endpoint interno para cancelaciones)
    @PutMapping("/{id}/increase-stock")
    public ResponseEntity<Map<String, Object>> increaseStock(@PathVariable Long id, @RequestParam Integer quantity) {
        logger.info("Petición para aumentar stock del producto {}: cantidad {}", id, quantity);

        try {
            boolean success = productService.increaseStock(id, quantity);

            Map<String, Object> response = new HashMap<>();
            response.put("success", success);
            response.put("message", success ? "Stock aumentado correctamente" : "No se pudo aumentar el stock");
            response.put("productId", id);
            response.put("quantityAdded", quantity);
            response.put("timestamp", LocalDateTime.now());

            return new ResponseEntity<>(response, HttpStatus.OK);

        } catch (RuntimeException e) {
            logger.error("Error aumentando stock del producto {}: {}", id, e.getMessage());

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Error al aumentar stock");
            errorResponse.put("productId", id);
            errorResponse.put("error", e.getMessage());
            errorResponse.put("timestamp", LocalDateTime.now());

            return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
        }
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
