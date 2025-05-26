package com.project_final.product_service.service;

import com.project_final.product_service.model.Product;
import com.project_final.product_service.repositories.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
public class ProductService {

    @Autowired
    private ProductRepository productRepository;

    // Crear producto
    public Product createProduct(Product product) {
        if (product.getPrice().compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("El precio debe ser mayor que cero");
        }
        if (product.getStock() < 0) {
            throw new RuntimeException("El stock no puede ser negativo");
        }
        return productRepository.save(product);
    }

    // Obtener todos los productos
    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    // Obtener producto por ID
    public Optional<Product> getProductById(Long id) {
        return productRepository.findById(id);
    }

    // Actualizar producto
    public Product updateProduct(Long id, Product productDetails) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Producto no encontrado con id: " + id));

        product.setName(productDetails.getName());
        product.setDescription(productDetails.getDescription());
        product.setPrice(productDetails.getPrice());
        product.setStock(productDetails.getStock());

        return productRepository.save(product);
    }

    // Eliminar producto
    public void deleteProduct(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Producto no encontrado con id: " + id));

        productRepository.delete(product);
    }

    // Buscar productos por nombre
    public List<Product> searchProductsByName(String name) {
        return productRepository.findByNameContaining(name);
    }

    // Obtener productos disponibles (con stock)
    public List<Product> getAvailableProducts() {
        return productRepository.findAvailableProducts();
    }

    // Buscar productos por rango de precio
    public List<Product> getProductsByPriceRange(BigDecimal minPrice, BigDecimal maxPrice) {
        return productRepository.findByPriceRange(minPrice, maxPrice);
    }

    // Obtener productos con stock bajo
    public List<Product> getLowStockProducts(Integer threshold) {
        return productRepository.findLowStockProducts(threshold != null ? threshold : 10);
    }

    // Reducir stock de un producto (usado por Order Service)
    @Transactional
    public boolean reduceStock(Long productId, Integer quantity) {
        try {
            Product product = productRepository.findById(productId)
                    .orElseThrow(() -> new RuntimeException("Producto no encontrado con id: " + productId));

            product.reduceStock(quantity);
            productRepository.save(product);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    // Aumentar stock de un producto (para cancelaciones)
    @Transactional
    public boolean increaseStock(Long productId, Integer quantity) {
        try {
            Product product = productRepository.findById(productId)
                    .orElseThrow(() -> new RuntimeException("Producto no encontrado con id: " + productId));

            product.increaseStock(quantity);
            productRepository.save(product);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    // Verificar stock disponible
    public boolean hasEnoughStock(Long productId, Integer quantity) {
        return productRepository.hasEnoughStock(productId, quantity);
    }

    // Obtener estadísticas
    public Long getTotalProducts() {
        return productRepository.countAllProducts();
    }

    public Long getAvailableProductsCount() {
        return productRepository.countAvailableProducts();
    }
}
