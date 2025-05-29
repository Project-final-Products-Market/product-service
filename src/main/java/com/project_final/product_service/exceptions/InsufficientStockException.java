package com.project_final.product_service.exceptions;

public class InsufficientStockException extends RuntimeException {
    private final Long productId;
    private final Integer availableStock;
    private final Integer requestedQuantity;

    public InsufficientStockException(Long productId, Integer availableStock, Integer requestedQuantity) {
        super("Stock insuficiente para el producto con ID " + productId +
                ". Stock disponible: " + availableStock +
                ", cantidad solicitada: " + requestedQuantity);
        this.productId = productId;
        this.availableStock = availableStock;
        this.requestedQuantity = requestedQuantity;
    }

    public Long getProductId() {
        return productId;
    }

    public Integer getAvailableStock() {
        return availableStock;
    }

    public Integer getRequestedQuantity() {
        return requestedQuantity;
    }
}
