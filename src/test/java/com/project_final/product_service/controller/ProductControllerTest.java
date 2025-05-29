package com.project_final.product_service.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.project_final.product_service.model.Product;
import com.project_final.product_service.service.ProductService;
import com.project_final.product_service.exceptions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ProductController.class)
class ProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ProductService productService;

    @Autowired
    private ObjectMapper objectMapper;

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

    //  TESTS POST /api/products

    @Test
    void createProduct_ValidProduct_ReturnsCreatedProduct() throws Exception {
        // Arrange
        when(productService.createProduct(any(Product.class))).thenReturn(testProductWithId);

        // Act & Assert
        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testProduct)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Producto creado correctamente"))
                .andExpect(jsonPath("$.product.id").value(1))
                .andExpect(jsonPath("$.product.name").value("Test Product"))
                .andExpect(jsonPath("$.product.price").value(99.99))
                .andExpect(jsonPath("$.product.stock").value(50));

        verify(productService).createProduct(any(Product.class));
    }

    @Test
    void createProduct_InvalidProduct_ReturnsBadRequest() throws Exception {
        // Arrange
        when(productService.createProduct(any(Product.class)))
                .thenThrow(ProductValidationException.emptyName());

        // Act & Assert
        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testProduct)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Error al crear producto"));

        verify(productService).createProduct(any(Product.class));
    }

    //  TESTS GET /api/products

    @Test
    void getAllProducts_ReturnsProductList() throws Exception {
        // Arrange
        List<Product> products = Arrays.asList(testProductWithId, testProductWithId);
        when(productService.getAllProducts()).thenReturn(products);

        // Act & Assert
        mockMvc.perform(get("/api/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].name").value("Test Product"));

        verify(productService).getAllProducts();
    }

    //  TESTS GET /api/products/{id}

    @Test
    void getProductById_ExistingId_ReturnsProduct() throws Exception {
        // Arrange
        when(productService.getProductById(1L)).thenReturn(Optional.of(testProductWithId));

        // Act & Assert
        mockMvc.perform(get("/api/products/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Test Product"));

        verify(productService).getProductById(1L);
    }

    @Test
    void getProductById_NonExistingId_ReturnsNotFound() throws Exception {
        // Arrange
        when(productService.getProductById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        mockMvc.perform(get("/api/products/999"))
                .andExpect(status().isNotFound());

        verify(productService).getProductById(999L);
    }

    //  TESTS PUT /api/products/{id}

    @Test
    void updateProduct_ValidData_ReturnsUpdatedProduct() throws Exception {
        // Arrange
        Product updatedProduct = new Product();
        updatedProduct.setId(1L);
        updatedProduct.setName("Updated Product");
        updatedProduct.setPrice(new BigDecimal("199.99"));
        updatedProduct.setStock(100);

        when(productService.updateProduct(eq(1L), any(Product.class))).thenReturn(updatedProduct);

        // Act & Assert
        mockMvc.perform(put("/api/products/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedProduct)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Producto actualizado correctamente"))
                .andExpect(jsonPath("$.product.name").value("Updated Product"))
                .andExpect(jsonPath("$.product.price").value(199.99));

        verify(productService).updateProduct(eq(1L), any(Product.class));
    }

    @Test
    void updateProduct_NonExistingId_ReturnsNotFound() throws Exception {
        // Arrange
        when(productService.updateProduct(eq(999L), any(Product.class)))
                .thenThrow(new ProductNotFoundException(999L));

        // Act & Assert
        mockMvc.perform(put("/api/products/999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testProduct)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Error al actualizar producto"));

        verify(productService).updateProduct(eq(999L), any(Product.class));
    }

    // TESTS DELETE /api/products/{id}

    @Test
    void deleteProduct_ExistingId_ReturnsSuccessMessage() throws Exception {
        // Arrange
        when(productService.getProductById(1L)).thenReturn(Optional.of(testProductWithId));
        doNothing().when(productService).deleteProduct(1L);

        // Act & Assert
        mockMvc.perform(delete("/api/products/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Producto eliminado correctamente"))
                .andExpect(jsonPath("$.productId").value(1));

        verify(productService).deleteProduct(1L);
    }

    @Test
    void deleteProduct_NonExistingId_ReturnsNotFound() throws Exception {
        // Arrange - El controlador llama primero a getProductById y luego a deleteProduct
        when(productService.getProductById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        mockMvc.perform(delete("/api/products/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Producto no encontrado"))
                .andExpect(jsonPath("$.productId").value(999));

        // Verificar que solo se llamó a getProductById, no a deleteProduct
        verify(productService).getProductById(999L);
        verify(productService, never()).deleteProduct(999L);
    }

    //  TESTS GET /api/products/search

    @Test
    void searchProducts_ValidName_ReturnsMatchingProducts() throws Exception {
        // Arrange
        List<Product> products = Arrays.asList(testProductWithId);
        when(productService.searchProductsByName("Test")).thenReturn(products);

        // Act & Assert
        mockMvc.perform(get("/api/products/search")
                        .param("name", "Test"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].name").value("Test Product"));

        verify(productService).searchProductsByName("Test");
    }

    // TESTS GET /api/products/available

    @Test
    void getAvailableProducts_ReturnsAvailableProducts() throws Exception {
        // Arrange
        List<Product> products = Arrays.asList(testProductWithId);
        when(productService.getAvailableProducts()).thenReturn(products);

        // Act & Assert
        mockMvc.perform(get("/api/products/available"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));

        verify(productService).getAvailableProducts();
    }

    // TESTS GET /api/products/price-range

    @Test
    void getProductsByPriceRange_ValidRange_ReturnsProducts() throws Exception {
        // Arrange
        List<Product> products = Arrays.asList(testProductWithId);
        when(productService.getProductsByPriceRange(any(BigDecimal.class), any(BigDecimal.class)))
                .thenReturn(products);

        // Act & Assert
        mockMvc.perform(get("/api/products/price-range")
                        .param("minPrice", "50.00")
                        .param("maxPrice", "150.00"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));

        verify(productService).getProductsByPriceRange(any(BigDecimal.class), any(BigDecimal.class));
    }

    //  TESTS GET /api/products/low-stock

    @Test
    void getLowStockProducts_ReturnsLowStockProducts() throws Exception {
        // Arrange
        List<Product> products = Arrays.asList(testProductWithId);
        when(productService.getLowStockProducts(10)).thenReturn(products);

        // Act & Assert
        mockMvc.perform(get("/api/products/low-stock")
                        .param("threshold", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));

        verify(productService).getLowStockProducts(10);
    }

    //  TESTS PUT /api/products/{id}/reduce-stock

    @Test
    void reduceStock_ValidOperation_ReturnsSuccessResponse() throws Exception {
        // Arrange
        when(productService.reduceStock(1L, 10)).thenReturn(true);

        // Act & Assert
        mockMvc.perform(put("/api/products/1/reduce-stock")
                        .param("quantity", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Stock reducido correctamente"));

        verify(productService).reduceStock(1L, 10);
    }

    @Test
    void reduceStock_InsufficientStock_ReturnsBadRequest() throws Exception {
        // Arrange
        when(productService.reduceStock(1L, 100))
                .thenThrow(new InsufficientStockException(1L, 50, 100));

        // Act & Assert
        mockMvc.perform(put("/api/products/1/reduce-stock")
                        .param("quantity", "100"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Error al reducir stock"));

        verify(productService).reduceStock(1L, 100);
    }

    //  TESTS PUT /api/products/{id}/increase-stock

    @Test
    void increaseStock_ValidOperation_ReturnsSuccessResponse() throws Exception {
        // Arrange
        when(productService.increaseStock(1L, 20)).thenReturn(true);

        // Act & Assert
        mockMvc.perform(put("/api/products/1/increase-stock")
                        .param("quantity", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Stock aumentado correctamente"));

        verify(productService).increaseStock(1L, 20);
    }

    //  TESTS GET /api/products/{id}/check-stock

    @Test
    void checkStock_SufficientStock_ReturnsTrue() throws Exception {
        // Arrange
        when(productService.hasEnoughStock(1L, 10)).thenReturn(true);

        // Act & Assert
        mockMvc.perform(get("/api/products/1/check-stock")
                        .param("quantity", "10"))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));

        verify(productService).hasEnoughStock(1L, 10);
    }

    @Test
    void checkStock_InsufficientStock_ReturnsFalse() throws Exception {
        // Arrange
        when(productService.hasEnoughStock(1L, 100)).thenReturn(false);

        // Act & Assert
        mockMvc.perform(get("/api/products/1/check-stock")
                        .param("quantity", "100"))
                .andExpect(status().isOk())
                .andExpect(content().string("false"));

        verify(productService).hasEnoughStock(1L, 100);
    }

    // TESTS DE ESTADÍSTICAS

    @Test
    void getTotalProducts_ReturnsCount() throws Exception {
        // Arrange
        when(productService.getTotalProducts()).thenReturn(5L);

        // Act & Assert
        mockMvc.perform(get("/api/products/stats/total"))
                .andExpect(status().isOk())
                .andExpect(content().string("5"));

        verify(productService).getTotalProducts();
    }

    @Test
    void getAvailableProductsCount_ReturnsCount() throws Exception {
        // Arrange
        when(productService.getAvailableProductsCount()).thenReturn(3L);

        // Act & Assert
        mockMvc.perform(get("/api/products/stats/available"))
                .andExpect(status().isOk())
                .andExpect(content().string("3"));

        verify(productService).getAvailableProductsCount();
    }
}