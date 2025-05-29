package com.project_final.product_service.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.project_final.product_service.ProductServiceApplication;
import com.project_final.product_service.model.Product;
import com.project_final.product_service.repositories.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for Product Service
 * Tests the complete flow from REST endpoints to database operations
 */
@SpringBootTest(
        classes = ProductServiceApplication.class,
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public class ProductServiceIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private String baseUrl;
    private Product testProduct;
    private HttpHeaders headers;

    @BeforeEach
    void setUp() {
        baseUrl = "http://localhost:" + port + "/api/products";

        // Setup HTTP headers
        headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        // Create test product
        testProduct = createTestProduct();
    }

    @Test
    @Order(1)
    void contextLoads() {
        assertThat(restTemplate).isNotNull();
        assertThat(productRepository).isNotNull();
    }

    @Test
    @Order(2)
    void createProduct_ShouldReturnCreatedProduct() {
        // Given
        HttpEntity<Map<String, Object>> request = new HttpEntity<>(Map.of(
                "name", testProduct.getName(),
                "description", testProduct.getDescription(),
                "price", testProduct.getPrice(),
                "stock", testProduct.getStock()
        ), headers);

        // When - Cambiar a esperar la estructura de respuesta del controlador
        ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                baseUrl,
                HttpMethod.POST,
                request,
                new ParameterizedTypeReference<Map<String, Object>>() {}
        );

        // Then - CORREGIDO: Verificar estructura JSON de respuesta del controlador
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().get("success")).isEqualTo(true);
        assertThat(response.getBody().get("message")).isEqualTo("Producto creado correctamente");

        @SuppressWarnings("unchecked")
        Map<String, Object> product = (Map<String, Object>) response.getBody().get("product");
        assertThat(product.get("name")).isEqualTo(testProduct.getName());
        assertThat(product.get("price")).isEqualTo(testProduct.getPrice().doubleValue());
        assertThat(product.get("stock")).isEqualTo(testProduct.getStock());
        assertThat(product.get("id")).isNotNull();

        // Verify database persistence
        Long productId = ((Number) product.get("id")).longValue();
        Optional<Product> savedProduct = productRepository.findById(productId);
        assertThat(savedProduct).isPresent();
        assertThat(savedProduct.get().getName()).isEqualTo(testProduct.getName());
    }

    @Test
    @Order(3)
    void getAllProducts_ShouldReturnProductList() {
        // Given - Save test products
        Product product1 = productRepository.save(createTestProduct("Laptop Gaming", "Laptop para gaming de alta gama"));
        Product product2 = productRepository.save(createTestProduct("Mouse Wireless", "Mouse inalámbrico ergonómico"));

        // When
        ResponseEntity<List<Product>> response = restTemplate.exchange(
                baseUrl,
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<List<Product>>() {}
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().size()).isGreaterThanOrEqualTo(2);

        List<String> productNames = response.getBody().stream()
                .map(Product::getName)
                .toList();
        assertThat(productNames).contains("Laptop Gaming", "Mouse Wireless");
    }

    @Test
    @Order(4)
    void getProductById_WhenProductExists_ShouldReturnProduct() {
        // Given
        Product savedProduct = productRepository.save(testProduct);
        Long productId = savedProduct.getId();

        // When
        ResponseEntity<Product> response = restTemplate.getForEntity(
                baseUrl + "/" + productId, Product.class
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getId()).isEqualTo(productId);
        assertThat(response.getBody().getName()).isEqualTo(savedProduct.getName());
    }

    @Test
    @Order(5)
    void getProductById_WhenProductNotExists_ShouldReturnNotFound() {
        // Given
        Long nonExistentId = 99999L;

        // When
        ResponseEntity<Product> response = restTemplate.getForEntity(
                baseUrl + "/" + nonExistentId, Product.class
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    @Order(6)
    void updateProduct_WhenProductExists_ShouldReturnUpdatedProduct() {
        // Given
        Product savedProduct = productRepository.save(testProduct);
        Long productId = savedProduct.getId();

        Map<String, Object> updateProduct = Map.of(
                "name", "Producto Actualizado",
                "description", "Descripción actualizada",
                "price", 999.99,
                "stock", 150
        );

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(updateProduct, headers);

        // When - CORREGIDO: Cambiar a esperar estructura JSON de respuesta
        ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                baseUrl + "/" + productId,
                HttpMethod.PUT,
                request,
                new ParameterizedTypeReference<Map<String, Object>>() {}
        );

        // Then - CORREGIDO: Verificar estructura JSON de respuesta
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().get("success")).isEqualTo(true);
        assertThat(response.getBody().get("message")).isEqualTo("Producto actualizado correctamente");

        @SuppressWarnings("unchecked")
        Map<String, Object> product = (Map<String, Object>) response.getBody().get("product");
        assertThat(product.get("name")).isEqualTo("Producto Actualizado");
        assertThat(product.get("price")).isEqualTo(999.99);

        // Verify database update
        Optional<Product> updatedProduct = productRepository.findById(productId);
        assertThat(updatedProduct).isPresent();
        assertThat(updatedProduct.get().getName()).isEqualTo("Producto Actualizado");
    }

    @Test
    @Order(7)
    void deleteProduct_WhenProductExists_ShouldReturnOk() {
        // Given
        Product savedProduct = productRepository.save(testProduct);
        Long productId = savedProduct.getId();

        // When - CORREGIDO: Cambiar a esperar estructura JSON de respuesta
        ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                baseUrl + "/" + productId,
                HttpMethod.DELETE,
                null,
                new ParameterizedTypeReference<Map<String, Object>>() {}
        );

        // Then - CORREGIDO: Esperar 200 OK en lugar de 204 NO_CONTENT
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().get("success")).isEqualTo(true);
        assertThat(response.getBody().get("message")).isEqualTo("Producto eliminado correctamente");

        // Verify product is deleted from database
        Optional<Product> deletedProduct = productRepository.findById(productId);
        assertThat(deletedProduct).isEmpty();
    }

    @Test
    @Order(8)
    void searchProductsByName_ShouldReturnFilteredProducts() {
        // Given
        Product laptop = createTestProduct("MacBook Pro", "Laptop profesional");
        Product mouse = createTestProduct("Magic Mouse", "Mouse Apple");
        Product keyboard = createTestProduct("Magic Keyboard", "Teclado Apple");

        productRepository.saveAll(List.of(laptop, mouse, keyboard));

        // When
        ResponseEntity<List<Product>> response = restTemplate.exchange(
                baseUrl + "/search?name=Magic",
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<List<Product>>() {}
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().size()).isEqualTo(2);

        List<String> productNames = response.getBody().stream()
                .map(Product::getName)
                .toList();
        assertThat(productNames).contains("Magic Mouse", "Magic Keyboard");
    }

    @Test
    @Order(9)
    void getAvailableProducts_ShouldReturnOnlyProductsWithStock() {
        // Given
        Product availableProduct = createTestProduct("Producto Disponible", "Con stock");
        availableProduct.setStock(50);

        Product outOfStockProduct = createTestProduct("Producto Agotado", "Sin stock");
        outOfStockProduct.setStock(0);

        productRepository.saveAll(List.of(availableProduct, outOfStockProduct));

        // When
        ResponseEntity<List<Product>> response = restTemplate.exchange(
                baseUrl + "/available",
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<List<Product>>() {}
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();

        // Verify all returned products have stock > 0
        response.getBody().forEach(product ->
                assertThat(product.getStock()).isGreaterThan(0)
        );

        List<String> productNames = response.getBody().stream()
                .map(Product::getName)
                .toList();
        assertThat(productNames).contains("Producto Disponible");
        assertThat(productNames).doesNotContain("Producto Agotado");
    }

    @Test
    @Order(10)
    void getProductsByPriceRange_ShouldReturnFilteredProducts() {
        // Given
        Product cheapProduct = createTestProduct("Producto Barato", "Precio bajo");
        cheapProduct.setPrice(new BigDecimal("10.00"));

        Product expensiveProduct = createTestProduct("Producto Caro", "Precio alto");
        expensiveProduct.setPrice(new BigDecimal("1000.00"));

        Product midRangeProduct = createTestProduct("Producto Medio", "Precio medio");
        midRangeProduct.setPrice(new BigDecimal("500.00"));

        productRepository.saveAll(List.of(cheapProduct, expensiveProduct, midRangeProduct));

        // When - Search for products between 100 and 600
        ResponseEntity<List<Product>> response = restTemplate.exchange(
                baseUrl + "/price-range?minPrice=100&maxPrice=600",
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<List<Product>>() {}
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();

        response.getBody().forEach(product -> {
            assertThat(product.getPrice()).isGreaterThanOrEqualTo(new BigDecimal("100"));
            assertThat(product.getPrice()).isLessThanOrEqualTo(new BigDecimal("600"));
        });
    }

    @Test
    @Order(11)
    void getLowStockProducts_ShouldReturnProductsWithLowStock() {
        // Given
        Product lowStockProduct = createTestProduct("Stock Bajo", "Producto con poco stock");
        lowStockProduct.setStock(5);

        Product highStockProduct = createTestProduct("Stock Alto", "Producto con mucho stock");
        highStockProduct.setStock(100);

        productRepository.saveAll(List.of(lowStockProduct, highStockProduct));

        // When - Search for products with stock less than 10
        ResponseEntity<List<Product>> response = restTemplate.exchange(
                baseUrl + "/low-stock?threshold=10",
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<List<Product>>() {}
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();

        response.getBody().forEach(product ->
                assertThat(product.getStock()).isLessThan(10)
        );

        List<String> productNames = response.getBody().stream()
                .map(Product::getName)
                .toList();
        assertThat(productNames).contains("Stock Bajo");
        assertThat(productNames).doesNotContain("Stock Alto");
    }

    @Test
    @Order(12)
    void reduceStock_WhenProductExists_ShouldReduceStockSuccessfully() {
        // Given
        Product product = testProduct;
        product.setStock(50);
        Product savedProduct = productRepository.save(product);
        Long productId = savedProduct.getId();
        Integer quantityToReduce = 10;

        // When - CORREGIDO: Cambiar el tipo de respuesta de Boolean a Map<String, Object>
        ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                baseUrl + "/" + productId + "/reduce-stock?quantity=" + quantityToReduce,
                HttpMethod.PUT,
                null,
                new ParameterizedTypeReference<Map<String, Object>>() {}
        );

        // Then - CORREGIDO: Verificar la estructura JSON de respuesta
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().get("success")).isEqualTo(true);
        assertThat(response.getBody().get("message")).isEqualTo("Stock reducido correctamente");

        // Verify stock was reduced in database
        Optional<Product> updatedProduct = productRepository.findById(productId);
        assertThat(updatedProduct).isPresent();
        assertThat(updatedProduct.get().getStock()).isEqualTo(40);
    }

    @Test
    @Order(13)
    void increaseStock_WhenProductExists_ShouldIncreaseStockSuccessfully() {
        // Given
        Product product = testProduct;
        product.setStock(30);
        Product savedProduct = productRepository.save(product);
        Long productId = savedProduct.getId();
        Integer quantityToIncrease = 20;

        // When - CORREGIDO: Cambiar el tipo de respuesta de Boolean a Map<String, Object>
        ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                baseUrl + "/" + productId + "/increase-stock?quantity=" + quantityToIncrease,
                HttpMethod.PUT,
                null,
                new ParameterizedTypeReference<Map<String, Object>>() {}
        );

        // Then - CORREGIDO: Verificar la estructura JSON de respuesta
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().get("success")).isEqualTo(true);
        assertThat(response.getBody().get("message")).isEqualTo("Stock aumentado correctamente");

        // Verify stock was increased in database
        Optional<Product> updatedProduct = productRepository.findById(productId);
        assertThat(updatedProduct).isPresent();
        assertThat(updatedProduct.get().getStock()).isEqualTo(50);
    }

    @Test
    @Order(14)
    void checkStock_WhenEnoughStock_ShouldReturnTrue() {
        // Given
        Product product = testProduct;
        product.setStock(100);
        Product savedProduct = productRepository.save(product);
        Long productId = savedProduct.getId();
        Integer quantityToCheck = 50;

        // When
        ResponseEntity<Boolean> response = restTemplate.getForEntity(
                baseUrl + "/" + productId + "/check-stock?quantity=" + quantityToCheck,
                Boolean.class
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isTrue();
    }

    @Test
    @Order(15)
    void checkStock_WhenInsufficientStock_ShouldReturnFalse() {
        // Given
        Product product = testProduct;
        product.setStock(10);
        Product savedProduct = productRepository.save(product);
        Long productId = savedProduct.getId();
        Integer quantityToCheck = 50;

        // When
        ResponseEntity<Boolean> response = restTemplate.getForEntity(
                baseUrl + "/" + productId + "/check-stock?quantity=" + quantityToCheck,
                Boolean.class
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isFalse();
    }

    @Test
    @Order(16)
    void getStatistics_ShouldReturnCorrectCounts() {
        // Given - Clear existing data and add test data
        productRepository.deleteAll();

        Product available1 = createTestProduct("Disponible 1", "Stock disponible");
        available1.setStock(10);
        Product available2 = createTestProduct("Disponible 2", "Stock disponible");
        available2.setStock(20);
        Product outOfStock = createTestProduct("Sin Stock", "Stock agotado");
        outOfStock.setStock(0);

        productRepository.saveAll(List.of(available1, available2, outOfStock));

        // When - Get total products
        ResponseEntity<Long> totalResponse = restTemplate.getForEntity(
                baseUrl + "/stats/total", Long.class
        );

        // When - Get available products count
        ResponseEntity<Long> availableResponse = restTemplate.getForEntity(
                baseUrl + "/stats/available", Long.class
        );

        // Then
        assertThat(totalResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(totalResponse.getBody()).isEqualTo(3L);

        assertThat(availableResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(availableResponse.getBody()).isEqualTo(2L);
    }

    @Test
    @Order(17)
    void createProductWithInvalidData_ShouldHandleValidationErrors() {
        // Given - Product with invalid data (null name)
        Map<String, Object> invalidProduct = Map.of(
                "name", "",  // Invalid: empty name
                "price", 100.00,
                "description", "Test Description",
                "stock", 10
        );

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(invalidProduct, headers);

        // When
        ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                baseUrl,
                HttpMethod.POST,
                request,
                new ParameterizedTypeReference<Map<String, Object>>() {}
        );

        // Then - Should handle validation error (actual behavior depends on error handling implementation)
        assertThat(response.getStatusCode()).isNotEqualTo(HttpStatus.CREATED);
    }

    @Test
    @Order(18)
    @Transactional
    void bulkOperations_ShouldHandleMultipleProducts() {
        // Given
        List<Product> products = List.of(
                createTestProduct("Bulk Item 1", "Descripción 1"),
                createTestProduct("Bulk Item 2", "Descripción 2"),
                createTestProduct("Bulk Item 3", "Descripción 3")
        );

        // When
        List<Product> savedProducts = productRepository.saveAll(products);

        // Then
        assertThat(savedProducts).hasSize(3);
        assertThat(productRepository.count()).isGreaterThanOrEqualTo(3);

        // Verify all products were saved with IDs
        savedProducts.forEach(product -> {
            assertThat(product.getId()).isNotNull();
            assertThat(product.getCreatedAt()).isNotNull();
            assertThat(product.getUpdatedAt()).isNotNull();
        });

        // Cleanup
        productRepository.deleteAll(savedProducts);
    }

    // Helper methods
    private Product createTestProduct() {
        return createTestProduct("Producto de Prueba", "Descripción del producto de prueba");
    }

    private Product createTestProduct(String name, String description) {
        Product product = new Product();
        product.setName(name);
        product.setDescription(description);
        product.setPrice(new BigDecimal("99.99"));
        product.setStock(100);
        return product;
    }
}
