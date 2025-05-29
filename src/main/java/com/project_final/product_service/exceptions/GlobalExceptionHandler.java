package com.project_final.product_service.exceptions;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    // ========== EXCEPCIONES ESPECÍFICAS DEL PRODUCTO ==========

    @ExceptionHandler(ProductNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleProductNotFoundException(
            ProductNotFoundException ex, WebRequest request) {

        logger.warn("Producto no encontrado: {}", ex.getMessage());

        ErrorResponse errorResponse = ErrorResponse.builder()
                .errorCode("PRODUCT_NOT_FOUND")
                .message(ex.getMessage())
                .details("El producto solicitado no existe en el sistema")
                .timestamp(LocalDateTime.now())
                .path(request.getDescription(false).replace("uri=", ""))
                .status(HttpStatus.NOT_FOUND.value())
                .build();

        return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(ProductValidationException.class)
    public ResponseEntity<ErrorResponse> handleProductValidationException(
            ProductValidationException ex, WebRequest request) {

        logger.warn("Error de validación de producto: {}", ex.getMessage());

        ErrorResponse errorResponse = ErrorResponse.builder()
                .errorCode("PRODUCT_VALIDATION_ERROR")
                .message(ex.getMessage())
                .details("Los datos del producto no son válidos")
                .timestamp(LocalDateTime.now())
                .path(request.getDescription(false).replace("uri=", ""))
                .status(HttpStatus.BAD_REQUEST.value())
                .field(ex.getField())
                .build();

        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(InsufficientStockException.class)
    public ResponseEntity<ErrorResponse> handleInsufficientStockException(
            InsufficientStockException ex, WebRequest request) {

        logger.warn("Stock insuficiente: {}", ex.getMessage());

        Map<String, Object> additionalInfo = new HashMap<>();
        additionalInfo.put("productId", ex.getProductId());
        additionalInfo.put("availableStock", ex.getAvailableStock());
        additionalInfo.put("requestedQuantity", ex.getRequestedQuantity());

        ErrorResponse errorResponse = ErrorResponse.builder()
                .errorCode("INSUFFICIENT_STOCK")
                .message(ex.getMessage())
                .details("No hay suficiente stock disponible para completar la operación")
                .timestamp(LocalDateTime.now())
                .path(request.getDescription(false).replace("uri=", ""))
                .status(HttpStatus.CONFLICT.value())
                .additionalInfo(additionalInfo)
                .build();

        return new ResponseEntity<>(errorResponse, HttpStatus.CONFLICT);
    }

    @ExceptionHandler(StockOperationException.class)
    public ResponseEntity<ErrorResponse> handleStockOperationException(
            StockOperationException ex, WebRequest request) {

        logger.error("Error del servicio de productos: {}", ex.getMessage());

        Map<String, Object> additionalInfo = new HashMap<>();
        additionalInfo.put("productId", ex.getProductId());
        additionalInfo.put("operation", ex.getOperation());

        ErrorResponse errorResponse = ErrorResponse.builder()
                .errorCode("STOCK_OPERATION_ERROR")
                .message(ex.getMessage())
                .details("Error interno del servicio de productos")
                .timestamp(LocalDateTime.now())
                .path(request.getDescription(false).replace("uri=", ""))
                .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .additionalInfo(additionalInfo)
                .build();

        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    // ========== EXCEPCIONES GENERALES DE SPRING ==========

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentNotValidException(
            MethodArgumentNotValidException ex, WebRequest request) {

        logger.warn("Error de validación: {}", ex.getMessage());

        StringBuilder message = new StringBuilder("Errores de validación: ");
        ex.getBindingResult().getFieldErrors().forEach(error -> {
            message.append(error.getField())
                    .append(" - ")
                    .append(error.getDefaultMessage())
                    .append("; ");
        });

        ErrorResponse errorResponse = ErrorResponse.builder()
                .errorCode("VALIDATION_ERROR")
                .message(message.toString())
                .details("Los datos proporcionados no son válidos")
                .timestamp(LocalDateTime.now())
                .path(request.getDescription(false).replace("uri=", ""))
                .status(HttpStatus.BAD_REQUEST.value())
                .build();

        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleHttpMessageNotReadableException(
            HttpMessageNotReadableException ex, WebRequest request) {

        logger.error("Error inesperado: {}", ex.getMessage());

        ErrorResponse errorResponse = ErrorResponse.builder()
                .errorCode("BAD_REQUEST")
                .message("Formato de datos inválido")
                .details("Los datos enviados no pueden ser procesados")
                .timestamp(LocalDateTime.now())
                .path(request.getDescription(false).replace("uri=", ""))
                .status(HttpStatus.BAD_REQUEST.value())
                .build();

        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ErrorResponse> handleMissingServletRequestParameterException(
            MissingServletRequestParameterException ex, WebRequest request) {

        logger.error("Error inesperado: {}", ex.getMessage());

        ErrorResponse errorResponse = ErrorResponse.builder()
                .errorCode("BAD_REQUEST")
                .message("Parámetro requerido faltante: " + ex.getParameterName())
                .details("Faltan parámetros obligatorios en la petición")
                .timestamp(LocalDateTime.now())
                .path(request.getDescription(false).replace("uri=", ""))
                .status(HttpStatus.BAD_REQUEST.value())
                .build();

        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentTypeMismatchException(
            MethodArgumentTypeMismatchException ex, WebRequest request) {

        logger.error("Error inesperado: {}", ex.getMessage());

        ErrorResponse errorResponse = ErrorResponse.builder()
                .errorCode("BAD_REQUEST")
                .message("Tipo de dato inválido para el parámetro: " + ex.getName())
                .details("El formato de los datos no es correcto")
                .timestamp(LocalDateTime.now())
                .path(request.getDescription(false).replace("uri=", "")) // ✅ CORREGIDO: Agregado .replace("uri=", "")
                .status(HttpStatus.BAD_REQUEST.value())
                .build();

        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    // ========== EXCEPCIÓN GENÉRICA ==========

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(
            Exception ex, WebRequest request) {

        logger.error("Error inesperado: {}", ex.getMessage());

        ErrorResponse errorResponse = ErrorResponse.builder()
                .errorCode("INTERNAL_SERVER_ERROR")
                .message("Error interno del servidor")
                .details("Error interno del servidor")
                .timestamp(LocalDateTime.now())
                .path(request.getDescription(false).replace("uri=", "")) // ✅ CORREGIDO: Agregado .replace("uri=", "")
                .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .build();

        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    // ========== CLASE PARA RESPUESTA DE ERROR ==========

    public static class ErrorResponse {
        private String errorCode;
        private String message;
        private String details;
        private LocalDateTime timestamp;
        private String path;
        private int status;
        private String field;
        private Map<String, Object> additionalInfo;

        // Constructor privado para el builder
        private ErrorResponse(Builder builder) {
            this.errorCode = builder.errorCode;
            this.message = builder.message;
            this.details = builder.details;
            this.timestamp = builder.timestamp;
            this.path = builder.path;
            this.status = builder.status;
            this.field = builder.field;
            this.additionalInfo = builder.additionalInfo;
        }

        // Builder pattern
        public static Builder builder() {
            return new Builder();
        }

        public static class Builder {
            private String errorCode;
            private String message;
            private String details;
            private LocalDateTime timestamp;
            private String path;
            private int status;
            private String field;
            private Map<String, Object> additionalInfo;

            public Builder errorCode(String errorCode) {
                this.errorCode = errorCode;
                return this;
            }

            public Builder message(String message) {
                this.message = message;
                return this;
            }

            public Builder details(String details) {
                this.details = details;
                return this;
            }

            public Builder timestamp(LocalDateTime timestamp) {
                this.timestamp = timestamp;
                return this;
            }

            public Builder path(String path) {
                this.path = path;
                return this;
            }

            public Builder status(int status) {
                this.status = status;
                return this;
            }

            public Builder field(String field) {
                this.field = field;
                return this;
            }

            public Builder additionalInfo(Map<String, Object> additionalInfo) {
                this.additionalInfo = additionalInfo;
                return this;
            }

            public ErrorResponse build() {
                return new ErrorResponse(this);
            }
        }

        // Getters
        public String getErrorCode() { return errorCode; }
        public String getMessage() { return message; }
        public String getDetails() { return details; }
        public LocalDateTime getTimestamp() { return timestamp; }
        public String getPath() { return path; }
        public int getStatus() { return status; }
        public String getField() { return field; }
        public Map<String, Object> getAdditionalInfo() { return additionalInfo; }
    }
}
