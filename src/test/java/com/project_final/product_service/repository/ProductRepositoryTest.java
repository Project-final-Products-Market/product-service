package com.project_final.product_service.repositories;

import com.project_final.product_service.model.Product;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
class ProductRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private ProductRepository productRepository;

    private Product product1;
    private Product product2;
    private Product product3;

    @BeforeEach
    void setUp() {
        // Producto con stock disponible
        product1 = new Product();
        product1.setName("Laptop Dell");
        product1.setDescription("Laptop para gaming");
        product1.setPrice(new BigDecimal("999.99"));
        product1.setStock(20);

        // Producto con poco stock
        product2 = new Product();
        product2.setName("Mouse Gamer");
        product2.setDescription("Mouse RGB");
        product2.setPrice(new BigDecimal("59.99"));
        product2.setStock(5);

        // Producto sin stock
        product3 = new Product();
        product3.setName("Teclado Mecánico");
        product3.setDescription("Teclado switch azul");
        product3.setPrice(new BigDecimal("129.99"));
        product3.setStock(0);

        // Guardar en la base de datos de prueba
        entityManager.persistAndFlush(product1);
        entityManager.persistAndFlush(product2);
        entityManager.persistAndFlush(product3);
    }

    //  TESTS DE BÚSQUEDA POR NOMBRE

    @Test
    void findByNameContaining_ExistingPartialName_ReturnsMatchingProducts() {
        // Act
        List<Product> result = productRepository.findByNameContaining("Laptop");

        // Assert
        assertEquals(1, result.size());
        assertEquals("Laptop Dell", result.get(0).getName());
    }

    @Test
    void findByNameContaining_CaseInsensitive_ReturnsMatchingProducts() {
        // Act
        List<Product> result = productRepository.findByNameContaining("MOUSE");

        // Assert
        assertEquals(1, result.size());
        assertEquals("Mouse Gamer", result.get(0).getName());
    }

    @Test
    void findByNameContaining_NonExistingName_ReturnsEmptyList() {
        // Act
        List<Product> result = productRepository.findByNameContaining("PlayStation");

        // Assert
        assertTrue(result.isEmpty());
    }

    @Test
    void findByNameContaining_EmptyString_ReturnsAllProducts() {
        // Act
        List<Product> result = productRepository.findByNameContaining("");

        // Assert
        assertEquals(3, result.size());
    }

    //  TESTS DE PRODUCTOS DISPONIBLES

    @Test
    void findAvailableProducts_ReturnsOnlyProductsWithStock() {
        // Act
        List<Product> result = productRepository.findAvailableProducts();

        // Assert
        assertEquals(2, result.size());
        assertTrue(result.stream().allMatch(p -> p.getStock() > 0));
        assertTrue(result.stream().anyMatch(p -> p.getName().equals("Laptop Dell")));
        assertTrue(result.stream().anyMatch(p -> p.getName().equals("Mouse Gamer")));
    }

    // TESTS DE RANGO DE PRECIOS

    @Test
    void findByPriceRange_ValidRange_ReturnsProductsInRange() {
        // Arrange
        BigDecimal minPrice = new BigDecimal("50.00");
        BigDecimal maxPrice = new BigDecimal("150.00");

        // Act
        List<Product> result = productRepository.findByPriceRange(minPrice, maxPrice);

        // Assert
        assertEquals(2, result.size());
        assertTrue(result.stream().anyMatch(p -> p.getName().equals("Mouse Gamer")));
        assertTrue(result.stream().anyMatch(p -> p.getName().equals("Teclado Mecánico")));
    }

    @Test
    void findByPriceRange_NoProductsInRange_ReturnsEmptyList() {
        // Arrange
        BigDecimal minPrice = new BigDecimal("2000.00");
        BigDecimal maxPrice = new BigDecimal("3000.00");

        // Act
        List<Product> result = productRepository.findByPriceRange(minPrice, maxPrice);

        // Assert
        assertTrue(result.isEmpty());
    }

    @Test
    void findByPriceRange_ExactPriceMatch_ReturnsProduct() {
        // Arrange
        BigDecimal exactPrice = new BigDecimal("59.99");

        // Act
        List<Product> result = productRepository.findByPriceRange(exactPrice, exactPrice);

        // Assert
        assertEquals(1, result.size());
        assertEquals("Mouse Gamer", result.get(0).getName());
    }

    //  TESTS DE STOCK BAJO

    @Test
    void findLowStockProducts_ThresholdTen_ReturnsProductsWithLowStock() {
        // Act
        List<Product> result = productRepository.findLowStockProducts(10);

        // Assert
        assertEquals(2, result.size());
        assertTrue(result.stream().anyMatch(p -> p.getName().equals("Mouse Gamer")));
        assertTrue(result.stream().anyMatch(p -> p.getName().equals("Teclado Mecánico")));
    }

    @Test
    void findLowStockProducts_ThresholdFive_ReturnsOnlyZeroStock() {
        // Act
        List<Product> result = productRepository.findLowStockProducts(5);

        // Assert
        assertEquals(1, result.size());
        assertEquals("Teclado Mecánico", result.get(0).getName());
        assertEquals(0, result.get(0).getStock());
    }

    @Test
    void findLowStockProducts_VeryHighThreshold_ReturnsAllProducts() {
        // Act
        List<Product> result = productRepository.findLowStockProducts(100);

        // Assert
        assertEquals(3, result.size());
    }

    // TESTS DE CONTEO

    @Test
    void countAvailableProducts_ReturnsCorrectCount() {
        // Act
        Long count = productRepository.countAvailableProducts();

        // Assert
        assertEquals(2L, count);
    }

    @Test
    void countAllProducts_ReturnsCorrectCount() {
        // Act
        Long count = productRepository.countAllProducts();

        // Assert
        assertEquals(3L, count);
    }

    //  TESTS DE VERIFICACIÓN DE STOCK

    @Test
    void hasEnoughStock_SufficientStock_ReturnsTrue() {
        // Arrange
        Long productId = product1.getId();

        // Act
        Boolean result = productRepository.hasEnoughStock(productId, 10);

        // Assert
        assertTrue(result);
    }

    @Test
    void hasEnoughStock_InsufficientStock_ReturnsFalse() {
        // Arrange
        Long productId = product1.getId();

        // Act
        Boolean result = productRepository.hasEnoughStock(productId, 50);

        // Assert
        assertFalse(result);
    }

    @Test
    void hasEnoughStock_ZeroStock_ReturnsFalse() {
        // Arrange
        Long productId = product3.getId();

        // Act
        Boolean result = productRepository.hasEnoughStock(productId, 1);

        // Assert
        assertFalse(result);
    }

    @Test
    void hasEnoughStock_ExactStock_ReturnsTrue() {
        // Arrange
        Long productId = product2.getId();

        // Act
        Boolean result = productRepository.hasEnoughStock(productId, 5);

        // Assert
        assertTrue(result);
    }

    @Test
    void hasEnoughStock_NonExistingProduct_ReturnsNull() {
        // Act
        Boolean result = productRepository.hasEnoughStock(999L, 1);

        // Assert
        assertNull(result);
    }

    // TESTS DE OPERACIONES CRUD BÁSICAS

    @Test
    void save_NewProduct_PersistsSuccessfully() {
        // Arrange
        Product newProduct = new Product();
        newProduct.setName("Nuevo Producto");
        newProduct.setDescription("Descripción del nuevo producto");
        newProduct.setPrice(new BigDecimal("199.99"));
        newProduct.setStock(15);

        // Act
        Product saved = productRepository.save(newProduct);
        entityManager.flush();

        // Assert
        assertNotNull(saved.getId());
        assertEquals("Nuevo Producto", saved.getName());

        // Verificar que se guardó en la base de datos
        Product found = entityManager.find(Product.class, saved.getId());
        assertNotNull(found);
        assertEquals("Nuevo Producto", found.getName());
    }

    @Test
    void findById_ExistingProduct_ReturnsProduct() {
        // Arrange
        Long productId = product1.getId();

        // Act
        Product found = productRepository.findById(productId).orElse(null);

        // Assert
        assertNotNull(found);
        assertEquals("Laptop Dell", found.getName());
        assertEquals(productId, found.getId());
    }

    @Test
    void delete_ExistingProduct_RemovesFromDatabase() {
        // Arrange
        Long productId = product1.getId();

        // Act
        productRepository.deleteById(productId);
        entityManager.flush();

        // Assert
        Product found = entityManager.find(Product.class, productId);
        assertNull(found);
    }

    // TESTS DE MÚLTIPLES PRODUCTOS

    @Test
    void findAll_ReturnsAllProducts() {
        // Act
        List<Product> allProducts = productRepository.findAll();

        // Assert
        assertEquals(3, allProducts.size());
        assertTrue(allProducts.stream().anyMatch(p -> p.getName().equals("Laptop Dell")));
        assertTrue(allProducts.stream().anyMatch(p -> p.getName().equals("Mouse Gamer")));
        assertTrue(allProducts.stream().anyMatch(p -> p.getName().equals("Teclado Mecánico")));
    }
}
