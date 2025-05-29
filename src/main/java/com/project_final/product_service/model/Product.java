package com.project_final.product_service.model;

import com.project_final.product_service.exceptions.InsufficientStockException;
import com.project_final.product_service.exceptions.ProductValidationException;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "products")
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "El nombre del producto es obligatorio")
    @Size(max = 255, message = "El nombre no puede tener más de 255 caracteres")
    @Column(nullable = false)
    private String name;

    @Size(max = 1000, message = "La descripción no puede tener más de 1000 caracteres")
    @Column(columnDefinition = "TEXT")
    private String description;

    @NotNull(message = "El precio es obligatorio")
    @DecimalMin(value = "0.01", message = "El precio debe ser mayor que cero")
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @NotNull(message = "El stock es obligatorio")
    @Min(value = 0, message = "El stock no puede ser negativo")
    @Column(nullable = false)
    private Integer stock;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Constructores
    public Product() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public Product(String name, String description, BigDecimal price, Integer stock) {
        this();
        this.name = name;
        this.description = description;
        this.price = price;
        this.stock = stock;
    }

    // Getters y Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
        this.updatedAt = LocalDateTime.now();
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
        this.updatedAt = LocalDateTime.now();
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        if (price != null && price.compareTo(BigDecimal.ZERO) <= 0) {
            throw new ProductValidationException("El precio debe ser mayor que cero");
        }
        this.price = price;
        this.updatedAt = LocalDateTime.now();
    }

    public Integer getStock() {
        return stock;
    }

    public void setStock(Integer stock) {
        if (stock != null && stock < 0) {
            throw new ProductValidationException("El stock no puede ser negativo");
        }
        this.stock = stock;
        this.updatedAt = LocalDateTime.now();
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    @PreUpdate
    public void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    // Método para reducir stock con validación mejorada
    public void reduceStock(Integer quantity) {
        if (quantity == null || quantity <= 0) {
            throw new ProductValidationException("La cantidad debe ser mayor que cero");
        }

        if (this.stock < quantity) {
            throw new InsufficientStockException(this.id, this.stock, quantity);
        }

        this.stock -= quantity;
        this.updatedAt = LocalDateTime.now();
    }

    // Método para aumentar stock con validación
    public void increaseStock(Integer quantity) {
        if (quantity == null || quantity <= 0) {
            throw new ProductValidationException("La cantidad debe ser mayor que cero");
        }

        this.stock += quantity;
        this.updatedAt = LocalDateTime.now();
    }

    // Método para verificar si hay stock suficiente
    public boolean hasEnoughStock(Integer quantity) {
        return quantity != null && quantity > 0 && this.stock >= quantity;
    }
}
