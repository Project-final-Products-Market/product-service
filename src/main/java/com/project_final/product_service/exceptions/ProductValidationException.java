package com.project_final.product_service.exceptions;

public class ProductValidationException extends RuntimeException {
    private final String field;

    public ProductValidationException(String message) {
        super(message);
        this.field = null;
    }

    public ProductValidationException(String field, String message) {
        super(message);
        this.field = field;
    }

    public String getField() {
        return field;
    }

    public static ProductValidationException emptyName() {
        return new ProductValidationException("name", "El nombre del producto no puede estar vacío");
    }

    public static ProductValidationException invalidPrice(String price) {
        return new ProductValidationException("price", "Precio inválido: " + price + ". El precio debe ser mayor que cero");
    }

    public static ProductValidationException negativeStock(Integer stock) {
        return new ProductValidationException("stock", "Stock no puede ser negativo: " + stock);
    }

    public static ProductValidationException invalidPriceRange() {
        return new ProductValidationException("El precio mínimo no puede ser mayor que el precio máximo");
    }
}