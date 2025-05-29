package com.project_final.product_service.model;

import com.project_final.product_service.exceptions.InsufficientStockException;
import com.project_final.product_service.exceptions.ProductValidationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class ProductModelTest {

    private Product product;

    @BeforeEach
    void setUp() {
        product = new Product();
        product.setName("Test Product");
        product.setDescription("Test Description");
        product.setPrice(new BigDecimal("99.99"));
        product.setStock(50);
    }

    //  TESTS DE CONSTRUCTORES

    @Test
    void defaultConstructor_SetsCreatedAtAndUpdatedAt() {
        // Arrange & Act
        Product newProduct = new Product();

        // Assert
        assertNotNull(newProduct.getCreatedAt());
        assertNotNull(newProduct.getUpdatedAt());
        assertTrue(newProduct.getCreatedAt().isBefore(LocalDateTime.now().plusSeconds(1)));
        assertTrue(newProduct.getUpdatedAt().isBefore(LocalDateTime.now().plusSeconds(1)));
    }

    @Test
    void parameterizedConstructor_SetsAllFieldsCorrectly() {
        // Arrange & Act
        Product newProduct = new Product("Test Product", "Test Description",
                new BigDecimal("99.99"), 50);

        // Assert
        assertEquals("Test Product", newProduct.getName());
        assertEquals("Test Description", newProduct.getDescription());
        assertEquals(new BigDecimal("99.99"), newProduct.getPrice());
        assertEquals(50, newProduct.getStock());
        assertNotNull(newProduct.getCreatedAt());
        assertNotNull(newProduct.getUpdatedAt());
    }

    // TESTS DE SETTERS CON VALIDACIÓN

    @Test
    void setPrice_ValidPrice_SetsSuccessfully() {
        // Arrange
        BigDecimal validPrice = new BigDecimal("199.99");
        LocalDateTime beforeUpdate = product.getUpdatedAt();

        // Act
        product.setPrice(validPrice);

        // Assert
        assertEquals(validPrice, product.getPrice());
        assertTrue(product.getUpdatedAt().isAfter(beforeUpdate));
    }

    @Test
    void setPrice_NegativePrice_ThrowsValidationException() {
        // Arrange
        BigDecimal negativePrice = new BigDecimal("-50.00");

        // Act & Assert
        ProductValidationException exception = assertThrows(
                ProductValidationException.class,
                () -> product.setPrice(negativePrice)
        );

        assertTrue(exception.getMessage().contains("precio"));
        assertTrue(exception.getMessage().contains("mayor que cero"));
    }

    @Test
    void setPrice_ZeroPrice_ThrowsValidationException() {
        // Arrange
        BigDecimal zeroPrice = BigDecimal.ZERO;

        // Act & Assert
        assertThrows(ProductValidationException.class,
                () -> product.setPrice(zeroPrice));
    }

    @Test
    void setPrice_NullPrice_DoesNotThrowException() {
        // Act & Assert
        assertDoesNotThrow(() -> product.setPrice(null));
        assertNull(product.getPrice());
    }

    @Test
    void setStock_ValidStock_SetsSuccessfully() {
        // Arrange
        Integer validStock = 100;
        LocalDateTime beforeUpdate = product.getUpdatedAt();

        // Act
        product.setStock(validStock);

        // Assert
        assertEquals(validStock, product.getStock());
        assertTrue(product.getUpdatedAt().isAfter(beforeUpdate));
    }

    @Test
    void setStock_NegativeStock_ThrowsValidationException() {
        // Arrange
        Integer negativeStock = -10;

        // Act & Assert
        ProductValidationException exception = assertThrows(
                ProductValidationException.class,
                () -> product.setStock(negativeStock)
        );

        assertTrue(exception.getMessage().contains("stock"));
        assertTrue(exception.getMessage().contains("no puede ser negativo"));
    }

    @Test
    void setStock_ZeroStock_SetsSuccessfully() {
        // Act & Assert
        assertDoesNotThrow(() -> product.setStock(0));
        assertEquals(0, product.getStock());
    }

    @Test
    void setName_UpdatesTimestamp() {
        // Arrange
        LocalDateTime beforeUpdate = product.getUpdatedAt();

        // Act
        product.setName("Updated Name");

        // Assert
        assertEquals("Updated Name", product.getName());
        assertTrue(product.getUpdatedAt().isAfter(beforeUpdate));
    }

    @Test
    void setDescription_UpdatesTimestamp() {
        // Arrange
        LocalDateTime beforeUpdate = product.getUpdatedAt();

        // Act
        product.setDescription("Updated Description");

        // Assert
        assertEquals("Updated Description", product.getDescription());
        assertTrue(product.getUpdatedAt().isAfter(beforeUpdate));
    }

    //  TESTS DE MÉTODOS DE STOCK

    @Test
    void reduceStock_ValidQuantity_ReducesSuccessfully() {
        // Arrange
        Integer initialStock = product.getStock();
        Integer quantityToReduce = 20;

        // Act
        product.reduceStock(quantityToReduce);

        // Assert
        assertEquals(initialStock - quantityToReduce, product.getStock());
        assertEquals(30, product.getStock());
    }

    @Test
    void reduceStock_ExactStock_ReducesToZero() {
        // Arrange
        Integer exactStock = product.getStock();

        // Act
        product.reduceStock(exactStock);

        // Assert
        assertEquals(0, product.getStock());
    }

    @Test
    void reduceStock_InsufficientStock_ThrowsInsufficientStockException() {
        // Arrange
        Integer excessiveQuantity = product.getStock() + 10;

        // Act & Assert
        InsufficientStockException exception = assertThrows(
                InsufficientStockException.class,
                () -> product.reduceStock(excessiveQuantity)
        );

        assertTrue(exception.getMessage().contains("Stock insuficiente"));
        assertTrue(exception.getMessage().contains(product.getStock().toString()));
        assertTrue(exception.getMessage().contains(excessiveQuantity.toString()));
    }

    @Test
    void reduceStock_NullQuantity_ThrowsValidationException() {
        // Act & Assert
        ProductValidationException exception = assertThrows(
                ProductValidationException.class,
                () -> product.reduceStock(null)
        );

        assertTrue(exception.getMessage().contains("cantidad"));
        assertTrue(exception.getMessage().contains("mayor que cero"));
    }

    @Test
    void reduceStock_ZeroQuantity_ThrowsValidationException() {
        // Act & Assert
        assertThrows(ProductValidationException.class,
                () -> product.reduceStock(0));
    }

    @Test
    void reduceStock_NegativeQuantity_ThrowsValidationException() {
        // Act & Assert
        assertThrows(ProductValidationException.class,
                () -> product.reduceStock(-5));
    }

    @Test
    void increaseStock_ValidQuantity_IncreasesSuccessfully() {
        // Arrange
        Integer initialStock = product.getStock();
        Integer quantityToAdd = 25;

        // Act
        product.increaseStock(quantityToAdd);

        // Assert
        assertEquals(initialStock + quantityToAdd, product.getStock());
        assertEquals(75, product.getStock());
    }

    @Test
    void increaseStock_NullQuantity_ThrowsValidationException() {
        // Act & Assert
        assertThrows(ProductValidationException.class,
                () -> product.increaseStock(null));
    }

    @Test
    void increaseStock_ZeroQuantity_ThrowsValidationException() {
        // Act & Assert
        assertThrows(ProductValidationException.class,
                () -> product.increaseStock(0));
    }

    @Test
    void increaseStock_NegativeQuantity_ThrowsValidationException() {
        // Act & Assert
        assertThrows(ProductValidationException.class,
                () -> product.increaseStock(-10));
    }

    @Test
    void hasEnoughStock_SufficientStock_ReturnsTrue() {
        // Act & Assert
        assertTrue(product.hasEnoughStock(30));
        assertTrue(product.hasEnoughStock(50)); // Exact stock
        assertTrue(product.hasEnoughStock(1));
    }

    @Test
    void hasEnoughStock_InsufficientStock_ReturnsFalse() {
        // Act & Assert
        assertFalse(product.hasEnoughStock(51));
        assertFalse(product.hasEnoughStock(100));
    }

    @Test
    void hasEnoughStock_NullQuantity_ReturnsFalse() {
        // Act & Assert
        assertFalse(product.hasEnoughStock(null));
    }

    @Test
    void hasEnoughStock_ZeroQuantity_ReturnsFalse() {
        // Act & Assert
        assertFalse(product.hasEnoughStock(0));
    }

    @Test
    void hasEnoughStock_NegativeQuantity_ReturnsFalse() {
        // Act & Assert
        assertFalse(product.hasEnoughStock(-5));
    }

    @Test
    void hasEnoughStock_ZeroStockProduct_ReturnsFalse() {
        // Arrange
        product.setStock(0);

        // Act & Assert
        assertFalse(product.hasEnoughStock(1));
        assertFalse(product.hasEnoughStock(10));
    }

    // TESTS DE MÉTODOS DE CICLO DE VIDA

    @Test
    void onUpdate_UpdatesTimestamp() throws InterruptedException {
        // Arrange
        LocalDateTime beforeUpdate = product.getUpdatedAt();

        // Simular una pequeña pausa para asegurar diferencia en timestamps
        Thread.sleep(10);

        // Act
        product.onUpdate(); // Llamada manual del método @PreUpdate

        // Assert
        assertTrue(product.getUpdatedAt().isAfter(beforeUpdate));
    }

    // TESTS DE VALIDACIONES INTEGRADAS

    @Test
    void stockOperations_UpdateTimestamp() {
        // Arrange
        LocalDateTime beforeOperation = product.getUpdatedAt();

        // Act
        product.reduceStock(10);

        // Assert
        assertTrue(product.getUpdatedAt().isAfter(beforeOperation));
        assertEquals(40, product.getStock());
    }

    @Test
    void multipleStockOperations_MaintainConsistency() {
        // Arrange
        Integer initialStock = product.getStock();

        // Act
        product.reduceStock(10);
        product.increaseStock(5);
        product.reduceStock(15);

        // Assert
        assertEquals(initialStock - 10 + 5 - 15, product.getStock());
        assertEquals(30, product.getStock());
    }

    @Test
    void edgeCases_StockOperations() {
        // Arrange
        product.setStock(1);

        // Act & Assert
        // Reducir todo el stock
        assertDoesNotThrow(() -> product.reduceStock(1));
        assertEquals(0, product.getStock());

        // Aumentar desde cero
        assertDoesNotThrow(() -> product.increaseStock(10));
        assertEquals(10, product.getStock());

        // Verificar que no hay stock suficiente para más de lo disponible
        assertFalse(product.hasEnoughStock(11));
        assertTrue(product.hasEnoughStock(10));
    }
}