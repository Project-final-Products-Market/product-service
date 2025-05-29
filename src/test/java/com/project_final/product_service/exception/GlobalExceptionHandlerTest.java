package com.project_final.product_service.exception;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.project_final.product_service.controller.ProductController;
import com.project_final.product_service.exceptions.*;
import com.project_final.product_service.model.Product;
import com.project_final.product_service.service.ProductService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ProductController.class)
class GlobalExceptionHandlerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ProductService productService;

    @Autowired
    private ObjectMapper objectMapper;

    private Product testProduct;

    @BeforeEach
    void setUp() {
        testProduct = new Product();
        testProduct.setName("Test Product");
        testProduct.setDescription("Test Description");
        testProduct.setPrice(new BigDecimal("99.99"));
        testProduct.setStock(50);
    }

    //  TESTS PARA ProductNotFoundException

    @Test
    void handleProductNotFoundException_ShouldReturnNotFound() throws Exception {
        // Given
        Long productId = 999L;
        when(productService.getProductById(productId))
                .thenThrow(new ProductNotFoundException(productId));

        // When & Then
        mockMvc.perform(get("/api/products/{id}", productId))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Producto no encontrado"))
                .andExpect(jsonPath("$.error").value("Producto con ID 999 no encontrado"))
                .andExpect(jsonPath("$.productId").value(999))
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.path").value("/api/products/999"));

        verify(productService).getProductById(productId);
    }

    @Test
    void handleProductNotFoundException_OnUpdate_ShouldReturnNotFound() throws Exception {
        // Given
        Long productId = 999L;
        when(productService.updateProduct(eq(productId), any(Product.class)))
                .thenThrow(new ProductNotFoundException(productId));

        // When & Then
        mockMvc.perform(put("/api/products/{id}", productId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testProduct)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Error al actualizar producto"))
                .andExpect(jsonPath("$.error").value("Producto con ID 999 no encontrado"))
                .andExpect(jsonPath("$.productId").value(999));

        verify(productService).updateProduct(eq(productId), any(Product.class));
    }

    @Test
    void handleProductNotFoundException_OnDelete_ShouldReturnNotFound() throws Exception {
        // Given
        Long productId = 999L;
        // El controlador llama a getProductById primero, no a deleteProduct
        when(productService.getProductById(productId))
                .thenReturn(Optional.empty());

        // When & Then
        mockMvc.perform(delete("/api/products/{id}", productId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Producto no encontrado"))
                .andExpect(jsonPath("$.productId").value(999))
                .andExpect(jsonPath("$.timestamp").exists());

        // Verificar que solo se llamó a getProductById, no a deleteProduct
        verify(productService).getProductById(productId);
        verify(productService, never()).deleteProduct(productId);
    }

    //  TESTS PARA ProductValidationException

    @Test
    void handleProductValidationException_EmptyName_ShouldReturnBadRequest() throws Exception {
        // Given
        when(productService.createProduct(any(Product.class)))
                .thenThrow(ProductValidationException.emptyName());

        // When & Then
        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testProduct)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Error al crear producto"))
                .andExpect(jsonPath("$.error").value("El nombre del producto no puede estar vacío"));

        verify(productService).createProduct(any(Product.class));
    }

    @Test
    void handleProductValidationException_InvalidPrice_ShouldReturnBadRequest() throws Exception {
        // Given
        String invalidPrice = "-10.00";
        when(productService.createProduct(any(Product.class)))
                .thenThrow(ProductValidationException.invalidPrice(invalidPrice));

        // When & Then
        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testProduct)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Error al crear producto"))
                .andExpect(jsonPath("$.error").value("Precio inválido: " + invalidPrice + ". El precio debe ser mayor que cero"));

        verify(productService).createProduct(any(Product.class));
    }

    @Test
    void handleProductValidationException_NegativeStock_ShouldReturnBadRequest() throws Exception {
        // Given
        Integer negativeStock = -5;
        when(productService.createProduct(any(Product.class)))
                .thenThrow(ProductValidationException.negativeStock(negativeStock));

        // When & Then
        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testProduct)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Error al crear producto"))
                .andExpect(jsonPath("$.error").value("Stock no puede ser negativo: " + negativeStock));

        verify(productService).createProduct(any(Product.class));
    }

    @Test
    void handleProductValidationException_InvalidPriceRange_ShouldReturnBadRequest() throws Exception {
        // Given
        when(productService.getProductsByPriceRange(any(BigDecimal.class), any(BigDecimal.class)))
                .thenThrow(ProductValidationException.invalidPriceRange());

        // When & Then
        mockMvc.perform(get("/api/products/price-range")
                        .param("minPrice", "100.00")
                        .param("maxPrice", "50.00"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Error de validación"))
                .andExpect(jsonPath("$.error").value("El precio mínimo no puede ser mayor que el precio máximo"));

        verify(productService).getProductsByPriceRange(any(BigDecimal.class), any(BigDecimal.class));
    }

    //  TESTS PARA InsufficientStockException

    @Test
    void handleInsufficientStockException_ShouldReturnBadRequest() throws Exception {
        // Given
        Long productId = 1L;
        Integer availableStock = 10;
        Integer requestedQuantity = 50;

        when(productService.reduceStock(productId, requestedQuantity))
                .thenThrow(new InsufficientStockException(productId, availableStock, requestedQuantity));

        // When & Then
        mockMvc.perform(put("/api/products/{id}/reduce-stock", productId)
                        .param("quantity", requestedQuantity.toString()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Error al reducir stock"))
                .andExpect(jsonPath("$.error").value("Stock insuficiente para el producto con ID 1. Stock disponible: 10, cantidad solicitada: 50"))
                .andExpect(jsonPath("$.productId").value(1));

        verify(productService).reduceStock(productId, requestedQuantity);
    }

    //  TESTS PARA StockOperationException

    @Test
    void handleStockOperationException_ReductionFailed_ShouldReturnBadRequest() throws Exception {
        // Given
        Long productId = 1L;
        Integer quantity = 10;
        RuntimeException cause = new RuntimeException("Database connection failed");

        when(productService.reduceStock(productId, quantity))
                .thenThrow(StockOperationException.reductionFailed(productId, cause));

        // When & Then
        mockMvc.perform(put("/api/products/{id}/reduce-stock", productId)
                        .param("quantity", quantity.toString()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Error al reducir stock"))
                .andExpect(jsonPath("$.error").value("Error al reducir stock del producto con ID 1"))
                .andExpect(jsonPath("$.productId").value(1));

        verify(productService).reduceStock(productId, quantity);
    }

    @Test
    void handleStockOperationException_IncreaseFailed_ShouldReturnBadRequest() throws Exception {
        // Given
        Long productId = 1L;
        Integer quantity = 20;
        RuntimeException cause = new RuntimeException("Database connection failed");

        when(productService.increaseStock(productId, quantity))
                .thenThrow(StockOperationException.increaseFailed(productId, cause));

        // When & Then
        mockMvc.perform(put("/api/products/{id}/increase-stock", productId)
                        .param("quantity", quantity.toString()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Error al aumentar stock"))
                .andExpect(jsonPath("$.error").value("Error al aumentar stock del producto con ID 1"))
                .andExpect(jsonPath("$.productId").value(1));

        verify(productService).increaseStock(productId, quantity);
    }

    //  TESTS PARA EXCEPCIONES GENERALES

    @Test
    void handleGenericException_ShouldReturnInternalServerError() throws Exception {
        // Given
        when(productService.getAllProducts())
                .thenThrow(new RuntimeException("Unexpected database error"));

        // When & Then
        mockMvc.perform(get("/api/products"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Error interno del servidor"))
                .andExpect(jsonPath("$.error").value("Ha ocurrido un error inesperado"))
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.path").value("/api/products"));

        verify(productService).getAllProducts();
    }

    @Test
    void handleMethodArgumentNotValidException_ShouldReturnBadRequest() throws Exception {
        // Given - Product with null required fields
        Product invalidProduct = new Product();
        // Solo establecemos campos opcionales, dejando los requeridos como null

        // When & Then
        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidProduct)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Error de validación"))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void handleHttpMessageNotReadableException_ShouldReturnBadRequest() throws Exception {
        // Given - Invalid JSON
        String invalidJson = "{ \"name\": \"Test\", \"price\": invalid_number }";

        // When & Then
        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Formato de datos inválido"))
                .andExpect(jsonPath("$.error").value("Los datos enviados no pueden ser procesados"))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void handleMissingServletRequestParameterException_ShouldReturnBadRequest() throws Exception {
        // Given - Missing required parameter
        // When & Then
        mockMvc.perform(get("/api/products/search"))  // Missing 'name' parameter
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Parámetro requerido faltante"))
                .andExpect(jsonPath("$.error").exists())
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void handleMethodArgumentTypeMismatchException_ShouldReturnBadRequest() throws Exception {
        // Given - Invalid parameter type (string instead of number)
        // When & Then
        mockMvc.perform(get("/api/products/invalid_id"))  // 'invalid_id' should be a Long
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Tipo de dato inválido"))
                .andExpect(jsonPath("$.error").exists())
                .andExpect(jsonPath("$.timestamp").exists());
    }
}