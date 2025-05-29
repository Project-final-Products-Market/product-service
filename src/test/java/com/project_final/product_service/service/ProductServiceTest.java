package com.project_final.product_service.service;

import com.project_final.product_service.model.Product;
import com.project_final.product_service.repositories.ProductRepository;
import com.project_final.product_service.exceptions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private ProductService productService;

    private Product testProduct;
    private Product testProductWithId;

    @BeforeEach
    void setUp() {
        testProduct = new Product();
        testProduct.setName("Test Product");
        testProduct.setDescription("Test Description");
        testProduct.setPrice(new BigDecimal("99.99"));
        testProduct.setStock(50);

        testProductWithId = new Product();
        testProductWithId.setId(1L);
        testProductWithId.setName("Test Product");
        testProductWithId.setDescription("Test Description");
        testProductWithId.setPrice(new BigDecimal("99.99"));
        testProductWithId.setStock(50);
        testProductWithId.setCreatedAt(LocalDateTime.now());
        testProductWithId.setUpdatedAt(LocalDateTime.now());
    }

    //  TESTS DE CREACIÓN

    @Test
    void createProduct_ValidProduct_ReturnsCreatedProduct() {
        // Arrange
        when(productRepository.save(any(Product.class))).thenReturn(testProductWithId);

        // Act
        Product result = productService.createProduct(testProduct);

        // Assert
        assertNotNull(result);
        assertEquals(testProduct.getName(), result.getName());
        assertEquals(testProduct.getPrice(), result.getPrice());
        verify(productRepository).save(testProduct);
    }

    @Test
    void createProduct_NullProduct_ThrowsValidationException() {
        // Act & Assert
        assertThrows(ProductValidationException.class,
                () -> productService.createProduct(null));

        verify(productRepository, never()).save(any());
    }

    @Test
    void createProduct_EmptyName_ThrowsValidationException() {
        // Arrange
        testProduct.setName("");

        // Act & Assert
        ProductValidationException exception = assertThrows(ProductValidationException.class,
                () -> productService.createProduct(testProduct));

        assertTrue(exception.getMessage().contains("nombre"));
        verify(productRepository, never()).save(any());
    }

    //  TESTS DE CONSULTA

    @Test
    void getAllProducts_ReturnsAllProducts() {
        // Arrange
        List<Product> products = Arrays.asList(testProductWithId, testProductWithId);
        when(productRepository.findAll()).thenReturn(products);

        // Act
        List<Product> result = productService.getAllProducts();

        // Assert
        assertEquals(2, result.size());
        verify(productRepository).findAll();
    }

    @Test
    void getProductById_ExistingId_ReturnsProduct() {
        // Arrange
        when(productRepository.findById(1L)).thenReturn(Optional.of(testProductWithId));

        // Act
        Optional<Product> result = productService.getProductById(1L);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(testProductWithId.getId(), result.get().getId());
        verify(productRepository).findById(1L);
    }

    @Test
    void getProductById_NonExistingId_ReturnsEmpty() {
        // Arrange
        when(productRepository.findById(999L)).thenReturn(Optional.empty());

        // Act
        Optional<Product> result = productService.getProductById(999L);

        // Assert
        assertFalse(result.isPresent());
        verify(productRepository).findById(999L);
    }

    //  TESTS DE ACTUALIZACIÓN

    @Test
    void updateProduct_ValidData_ReturnsUpdatedProduct() {
        // Arrange
        Product updatedData = new Product();
        updatedData.setName("Updated Product");
        updatedData.setDescription("Updated Description");
        updatedData.setPrice(new BigDecimal("199.99"));
        updatedData.setStock(100);

        when(productRepository.findById(1L)).thenReturn(Optional.of(testProductWithId));
        when(productRepository.save(any(Product.class))).thenReturn(testProductWithId);

        // Act
        Product result = productService.updateProduct(1L, updatedData);

        // Assert
        assertNotNull(result);
        verify(productRepository).findById(1L);
        verify(productRepository).save(any(Product.class));
    }

    @Test
    void updateProduct_NonExistingId_ThrowsNotFoundException() {
        // Arrange
        when(productRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ProductNotFoundException.class,
                () -> productService.updateProduct(999L, testProduct));

        verify(productRepository).findById(999L);
        verify(productRepository, never()).save(any());
    }

    // TESTS DE ELIMINACIÓN

    @Test
    void deleteProduct_ExistingId_DeletesProduct() {
        // Arrange
        when(productRepository.findById(1L)).thenReturn(Optional.of(testProductWithId));

        // Act
        productService.deleteProduct(1L);

        // Assert
        verify(productRepository).findById(1L);
        verify(productRepository).delete(testProductWithId);
    }

    @Test
    void deleteProduct_NonExistingId_ThrowsNotFoundException() {
        // Arrange
        when(productRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ProductNotFoundException.class,
                () -> productService.deleteProduct(999L));

        verify(productRepository).findById(999L);
        verify(productRepository, never()).delete(any());
    }

    //  TESTS DE BÚSQUEDA

    @Test
    void searchProductsByName_ValidName_ReturnsMatchingProducts() {
        // Arrange
        List<Product> products = Arrays.asList(testProductWithId);
        when(productRepository.findByNameContaining("Test")).thenReturn(products);

        // Act
        List<Product> result = productService.searchProductsByName("Test");

        // Assert
        assertEquals(1, result.size());
        verify(productRepository).findByNameContaining("Test");
    }

    @Test
    void searchProductsByName_EmptyName_ThrowsValidationException() {
        // Act & Assert
        assertThrows(ProductValidationException.class,
                () -> productService.searchProductsByName(""));

        verify(productRepository, never()).findByNameContaining(any());
    }

    //  TESTS DE STOCK

    @Test
    void reduceStock_ValidOperation_ReturnsTrue() {
        // Arrange
        when(productRepository.findById(1L)).thenReturn(Optional.of(testProductWithId));
        when(productRepository.save(any(Product.class))).thenReturn(testProductWithId);

        // Act
        boolean result = productService.reduceStock(1L, 10);

        // Assert
        assertTrue(result);
        verify(productRepository).findById(1L);
        verify(productRepository).save(any(Product.class));
    }

    @Test
    void reduceStock_InsufficientStock_ThrowsStockOperationException() {
        // Arrange
        when(productRepository.findById(1L)).thenReturn(Optional.of(testProductWithId));

        // Act & Assert - Corregido para esperar StockOperationException
        StockOperationException exception = assertThrows(StockOperationException.class,
                () -> productService.reduceStock(1L, 100)); // Más del stock disponible

        // Verificar que la causa original es InsufficientStockException
        assertTrue(exception.getCause() instanceof InsufficientStockException);
        assertTrue(exception.getMessage().contains("Error al reducir stock"));

        verify(productRepository).findById(1L);
    }

    @Test
    void reduceStock_ProductNotFound_ThrowsStockOperationException() {
        // Arrange
        when(productRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert - Corregido para esperar StockOperationException en lugar de ProductNotFoundException
        assertThrows(StockOperationException.class,
                () -> productService.reduceStock(999L, 5));

        verify(productRepository).findById(999L);
        verify(productRepository, never()).save(any());
    }

    @Test
    void increaseStock_ValidOperation_ReturnsTrue() {
        // Arrange
        when(productRepository.findById(1L)).thenReturn(Optional.of(testProductWithId));
        when(productRepository.save(any(Product.class))).thenReturn(testProductWithId);

        // Act
        boolean result = productService.increaseStock(1L, 20);

        // Assert
        assertTrue(result);
        verify(productRepository).findById(1L);
        verify(productRepository).save(any(Product.class));
    }

    @Test
    void increaseStock_ProductNotFound_ThrowsStockOperationException() {
        // Arrange
        when(productRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(StockOperationException.class,
                () -> productService.increaseStock(999L, 10));

        verify(productRepository).findById(999L);
        verify(productRepository, never()).save(any());
    }

    @Test
    void hasEnoughStock_SufficientStock_ReturnsTrue() {
        // Arrange
        when(productRepository.hasEnoughStock(1L, 10)).thenReturn(true);

        // Act
        boolean result = productService.hasEnoughStock(1L, 10);

        // Assert
        assertTrue(result);
        verify(productRepository).hasEnoughStock(1L, 10);
    }

    @Test
    void hasEnoughStock_InsufficientStock_ReturnsFalse() {
        // Arrange
        when(productRepository.hasEnoughStock(1L, 100)).thenReturn(false);

        // Act
        boolean result = productService.hasEnoughStock(1L, 100);

        // Assert
        assertFalse(result);
        verify(productRepository).hasEnoughStock(1L, 100);
    }

    @Test
    void hasEnoughStock_ProductNotFound_ReturnsFalse() {
        // Arrange
        when(productRepository.hasEnoughStock(999L, 10)).thenReturn(null);

        // Act
        boolean result = productService.hasEnoughStock(999L, 10);

        // Assert
        assertFalse(result);
        verify(productRepository).hasEnoughStock(999L, 10);
    }

    //  TESTS DE RANGO DE PRECIOS

    @Test
    void getProductsByPriceRange_ValidRange_ReturnsProducts() {
        // Arrange
        BigDecimal minPrice = new BigDecimal("50.00");
        BigDecimal maxPrice = new BigDecimal("150.00");
        List<Product> products = Arrays.asList(testProductWithId);

        when(productRepository.findByPriceRange(minPrice, maxPrice)).thenReturn(products);

        // Act
        List<Product> result = productService.getProductsByPriceRange(minPrice, maxPrice);

        // Assert
        assertEquals(1, result.size());
        verify(productRepository).findByPriceRange(minPrice, maxPrice);
    }

    @Test
    void getProductsByPriceRange_InvalidRange_ThrowsValidationException() {
        // Arrange
        BigDecimal minPrice = new BigDecimal("150.00");
        BigDecimal maxPrice = new BigDecimal("50.00");

        // Act & Assert
        assertThrows(ProductValidationException.class,
                () -> productService.getProductsByPriceRange(minPrice, maxPrice));
    }

    @Test
    void getProductsByPriceRange_NullPrices_ThrowsValidationException() {
        // Act & Assert
        assertThrows(ProductValidationException.class,
                () -> productService.getProductsByPriceRange(null, new BigDecimal("100.00")));

        assertThrows(ProductValidationException.class,
                () -> productService.getProductsByPriceRange(new BigDecimal("50.00"), null));
    }

    @Test
    void getProductsByPriceRange_NegativePrices_ThrowsValidationException() {
        // Arrange
        BigDecimal negativePrice = new BigDecimal("-10.00");
        BigDecimal validPrice = new BigDecimal("100.00");

        // Act & Assert
        assertThrows(ProductValidationException.class,
                () -> productService.getProductsByPriceRange(negativePrice, validPrice));
    }

    //  TESTS DE STOCK BAJO

    @Test
    void getLowStockProducts_ValidThreshold_ReturnsProducts() {
        // Arrange
        List<Product> products = Arrays.asList(testProductWithId);
        when(productRepository.findLowStockProducts(10)).thenReturn(products);

        // Act
        List<Product> result = productService.getLowStockProducts(10);

        // Assert
        assertEquals(1, result.size());
        verify(productRepository).findLowStockProducts(10);
    }

    @Test
    void getLowStockProducts_NullThreshold_UsesDefaultValue() {
        // Arrange
        List<Product> products = Arrays.asList(testProductWithId);
        when(productRepository.findLowStockProducts(10)).thenReturn(products);

        // Act
        List<Product> result = productService.getLowStockProducts(null);

        // Assert
        assertEquals(1, result.size());
        verify(productRepository).findLowStockProducts(10); // Default value
    }

    @Test
    void getLowStockProducts_NegativeThreshold_UsesDefaultValue() {
        // Arrange
        List<Product> products = Arrays.asList(testProductWithId);
        when(productRepository.findLowStockProducts(10)).thenReturn(products);

        // Act
        List<Product> result = productService.getLowStockProducts(-5);

        // Assert
        assertEquals(1, result.size());
        verify(productRepository).findLowStockProducts(10); // Default value
    }

    // TESTS DE ESTADÍSTICAS

    @Test
    void getTotalProducts_ReturnsCount() {
        // Arrange
        when(productRepository.countAllProducts()).thenReturn(5L);

        // Act
        Long result = productService.getTotalProducts();

        // Assert
        assertEquals(5L, result);
        verify(productRepository).countAllProducts();
    }

    @Test
    void getAvailableProductsCount_ReturnsCount() {
        // Arrange
        when(productRepository.countAvailableProducts()).thenReturn(3L);

        // Act
        Long result = productService.getAvailableProductsCount();

        // Assert
        assertEquals(3L, result);
        verify(productRepository).countAvailableProducts();
    }

    //  TESTS DE VALIDACIONES ADICIONALES

    @Test
    void reduceStock_NullProductId_ThrowsValidationException() {
        // Act & Assert
        assertThrows(ProductValidationException.class,
                () -> productService.reduceStock(null, 10));

        verify(productRepository, never()).findById(any());
    }

    @Test
    void reduceStock_NullQuantity_ThrowsValidationException() {
        // Act & Assert
        assertThrows(ProductValidationException.class,
                () -> productService.reduceStock(1L, null));

        verify(productRepository, never()).findById(any());
    }

    @Test
    void reduceStock_ZeroQuantity_ThrowsValidationException() {
        // Act & Assert
        assertThrows(ProductValidationException.class,
                () -> productService.reduceStock(1L, 0));

        verify(productRepository, never()).findById(any());
    }

    @Test
    void increaseStock_NullProductId_ThrowsValidationException() {
        // Act & Assert
        assertThrows(ProductValidationException.class,
                () -> productService.increaseStock(null, 10));

        verify(productRepository, never()).findById(any());
    }

    @Test
    void increaseStock_NullQuantity_ThrowsValidationException() {
        // Act & Assert
        assertThrows(ProductValidationException.class,
                () -> productService.increaseStock(1L, null));

        verify(productRepository, never()).findById(any());
    }

    @Test
    void increaseStock_ZeroQuantity_ThrowsValidationException() {
        // Act & Assert
        assertThrows(ProductValidationException.class,
                () -> productService.increaseStock(1L, 0));

        verify(productRepository, never()).findById(any());
    }

    @Test
    void hasEnoughStock_NullProductId_ThrowsValidationException() {
        // Act & Assert
        assertThrows(ProductValidationException.class,
                () -> productService.hasEnoughStock(null, 10));

        verify(productRepository, never()).hasEnoughStock(any(), any());
    }

    @Test
    void hasEnoughStock_NullQuantity_ThrowsValidationException() {
        // Act & Assert
        assertThrows(ProductValidationException.class,
                () -> productService.hasEnoughStock(1L, null));

        verify(productRepository, never()).hasEnoughStock(any(), any());
    }

    @Test
    void hasEnoughStock_ZeroQuantity_ThrowsValidationException() {
        // Act & Assert
        assertThrows(ProductValidationException.class,
                () -> productService.hasEnoughStock(1L, 0));

        verify(productRepository, never()).hasEnoughStock(any(), any());
    }
}