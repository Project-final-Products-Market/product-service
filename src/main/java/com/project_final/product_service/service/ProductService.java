package com.project_final.product_service.service;

import com.project_final.product_service.model.Product;
import com.project_final.product_service.repositories.ProductRepository;
import com.project_final.product_service.exceptions.*;
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
        validateProductData(product);
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
                .orElseThrow(() -> new ProductNotFoundException(id));

        validateProductData(productDetails);

        product.setName(productDetails.getName());
        product.setDescription(productDetails.getDescription());
        product.setPrice(productDetails.getPrice());
        product.setStock(productDetails.getStock());

        return productRepository.save(product);
    }

    // Eliminar producto
    public void deleteProduct(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException(id));

        productRepository.delete(product);
    }

    // Buscar productos por nombre
    public List<Product> searchProductsByName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new ProductValidationException("name", "El nombre de búsqueda no puede estar vacío");
        }
        return productRepository.findByNameContaining(name);
    }

    // Obtener productos disponibles (con stock)
    public List<Product> getAvailableProducts() {
        return productRepository.findAvailableProducts();
    }

    // Buscar productos por rango de precio
    public List<Product> getProductsByPriceRange(BigDecimal minPrice, BigDecimal maxPrice) {
        if (minPrice == null || maxPrice == null) {
            throw new ProductValidationException("Los precios mínimo y máximo son obligatorios");
        }

        if (minPrice.compareTo(BigDecimal.ZERO) < 0 || maxPrice.compareTo(BigDecimal.ZERO) < 0) {
            throw new ProductValidationException("Los precios no pueden ser negativos");
        }

        if (minPrice.compareTo(maxPrice) > 0) {
            throw ProductValidationException.invalidPriceRange();
        }

        return productRepository.findByPriceRange(minPrice, maxPrice);
    }

    // Obtener productos con stock bajo
    public List<Product> getLowStockProducts(Integer threshold) {
        if (threshold == null || threshold < 0) {
            threshold = 10; // valor por defecto
        }
        return productRepository.findLowStockProducts(threshold);
    }

    // Reducir stock de un producto (usado por Order Service)
    @Transactional
    public boolean reduceStock(Long productId, Integer quantity) {
        if (productId == null) {
            throw new ProductValidationException("productId", "El ID del producto no puede ser nulo");
        }

        if (quantity == null || quantity <= 0) {
            throw new ProductValidationException("quantity", "La cantidad debe ser mayor que cero");
        }

        try {
            Product product = productRepository.findById(productId)
                    .orElseThrow(() -> new ProductNotFoundException(productId));

            product.reduceStock(quantity);
            productRepository.save(product);
            return true;
        } catch (ProductServiceException e) {
            // Re-lanzar excepciones del servicio de productos
            throw e;
        } catch (Exception e) {
            throw StockOperationException.reductionFailed(productId, e);
        }
    }

    // Aumentar stock de un producto (para cancelaciones)
    @Transactional
    public boolean increaseStock(Long productId, Integer quantity) {
        if (productId == null) {
            throw new ProductValidationException("productId", "El ID del producto no puede ser nulo");
        }

        if (quantity == null || quantity <= 0) {
            throw new ProductValidationException("quantity", "La cantidad debe ser mayor que cero");
        }

        try {
            Product product = productRepository.findById(productId)
                    .orElseThrow(() -> new ProductNotFoundException(productId));

            product.increaseStock(quantity);
            productRepository.save(product);
            return true;
        } catch (ProductServiceException e) {
            // Re-lanzar excepciones del servicio de productos
            throw e;
        } catch (Exception e) {
            throw StockOperationException.increaseFailed(productId, e);
        }
    }

    // Verificar stock disponible
    public boolean hasEnoughStock(Long productId, Integer quantity) {
        if (productId == null) {
            throw new ProductValidationException("productId", "El ID del producto no puede ser nulo");
        }

        if (quantity == null || quantity <= 0) {
            throw new ProductValidationException("quantity", "La cantidad debe ser mayor que cero");
        }

        Boolean hasStock = productRepository.hasEnoughStock(productId, quantity);
        return hasStock != null && hasStock;
    }

    // Obtener estadísticas
    public Long getTotalProducts() {
        return productRepository.countAllProducts();
    }

    public Long getAvailableProductsCount() {
        return productRepository.countAvailableProducts();
    }

    // Método privado para validar datos del producto
    private void validateProductData(Product product) {
        if (product == null) {
            throw new ProductValidationException("Los datos del producto no pueden ser nulos");
        }

        if (product.getName() == null || product.getName().trim().isEmpty()) {
            throw ProductValidationException.emptyName();
        }

        if (product.getPrice() == null) {
            throw new ProductValidationException("price", "El precio es obligatorio");
        }

        if (product.getPrice().compareTo(BigDecimal.ZERO) <= 0) {
            throw ProductValidationException.invalidPrice(product.getPrice().toString());
        }

        if (product.getStock() == null) {
            throw new ProductValidationException("stock", "El stock es obligatorio");
        }

        if (product.getStock() < 0) {
            throw ProductValidationException.negativeStock(product.getStock());
        }

        // Validación de longitud del nombre
        if (product.getName().length() > 255) {
            throw new ProductValidationException("name", "El nombre no puede tener más de 255 caracteres");
        }

        // Validación de descripción
        if (product.getDescription() != null && product.getDescription().length() > 1000) {
            throw new ProductValidationException("description", "La descripción no puede tener más de 1000 caracteres");
        }
    }
}