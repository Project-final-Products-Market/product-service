package com.project_final.product_service.exceptions;

public class StockOperationException extends RuntimeException {
    private final Long productId;
    private final String operation;

    public StockOperationException(String message, Long productId, String operation) {
        super(message);
        this.productId = productId;
        this.operation = operation;
    }

    public StockOperationException(String message, Long productId, String operation, Throwable cause) {
        super(message, cause);
        this.productId = productId;
        this.operation = operation;
    }

    public Long getProductId() {
        return productId;
    }

    public String getOperation() {
        return operation;
    }

    // Métodos estáticos para crear excepciones comunes
    public static StockOperationException reductionFailed(Long productId, Throwable cause) {
        return new StockOperationException(
                "Error al reducir stock del producto con ID " + productId,
                productId,
                "REDUCE",
                cause
        );
    }

    public static StockOperationException increaseFailed(Long productId, Throwable cause) {
        return new StockOperationException(
                "Error al aumentar stock del producto con ID " + productId,
                productId,
                "INCREASE",
                cause
        );
    }
}
