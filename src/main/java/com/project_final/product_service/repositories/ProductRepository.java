package com.project_final.product_service.repositories;

import com.project_final.product_service.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    // Buscar productos por nombre (contiene)
    @Query("SELECT p FROM Product p WHERE p.name LIKE %:name%")
    List<Product> findByNameContaining(@Param("name") String name);

    // Buscar productos con stock disponible
    @Query("SELECT p FROM Product p WHERE p.stock > 0")
    List<Product> findAvailableProducts();

    // Buscar productos por rango de precio
    @Query("SELECT p FROM Product p WHERE p.price BETWEEN :minPrice AND :maxPrice")
    List<Product> findByPriceRange(@Param("minPrice") BigDecimal minPrice, @Param("maxPrice") BigDecimal maxPrice);

    // Buscar productos con stock bajo (menos de X unidades)
    @Query("SELECT p FROM Product p WHERE p.stock < :threshold")
    List<Product> findLowStockProducts(@Param("threshold") Integer threshold);

    // Contar productos disponibles
    @Query("SELECT COUNT(p) FROM Product p WHERE p.stock > 0")
    Long countAvailableProducts();

    // Contar total de productos
    @Query("SELECT COUNT(p) FROM Product p")
    Long countAllProducts();

    // Verificar si hay stock suficiente
    @Query("SELECT CASE WHEN p.stock >= :quantity THEN true ELSE false END FROM Product p WHERE p.id = :productId")
    Boolean hasEnoughStock(@Param("productId") Long productId, @Param("quantity") Integer quantity);
}
