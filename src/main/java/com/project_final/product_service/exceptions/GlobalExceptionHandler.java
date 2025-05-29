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

    // EXCEPCIONES ESPECÍFICAS DEL PRODUCTO - FORMATO CONSISTENTE CON EL CONTROLADOR

    @ExceptionHandler(ProductNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleProductNotFoundException(
            ProductNotFoundException ex, WebRequest request) {

        logger.warn("Producto no encontrado: {}", ex.getMessage());

        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("success", false);
        errorResponse.put("message", "Producto no encontrado");
        errorResponse.put("error", ex.getMessage());
        errorResponse.put("productId", ex.getProductId());
        errorResponse.put("timestamp", LocalDateTime.now());
        errorResponse.put("path", request.getDescription(false).replace("uri=", ""));

        return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(ProductValidationException.class)
    public ResponseEntity<Map<String, Object>> handleProductValidationException(
            ProductValidationException ex, WebRequest request) {

        logger.warn("Error de validación de producto: {}", ex.getMessage());

        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("success", false);
        errorResponse.put("message", "Error de validación");
        errorResponse.put("error", ex.getMessage());

        // Solo agregar el campo si existe
        if (ex.getField() != null) {
            errorResponse.put("field", ex.getField());
        }

        errorResponse.put("timestamp", LocalDateTime.now());
        errorResponse.put("path", request.getDescription(false).replace("uri=", ""));

        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(InsufficientStockException.class)
    public ResponseEntity<Map<String, Object>> handleInsufficientStockException(
            InsufficientStockException ex, WebRequest request) {

        logger.warn("Stock insuficiente: {}", ex.getMessage());

        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("success", false);
        errorResponse.put("message", "Stock insuficiente");
        errorResponse.put("error", ex.getMessage());

        // Solo agregar campos si los métodos existen
        try {
            errorResponse.put("productId", ex.getProductId());
        } catch (Exception ignored) {}

        try {
            errorResponse.put("availableStock", ex.getAvailableStock());
        } catch (Exception ignored) {}

        try {
            errorResponse.put("requestedQuantity", ex.getRequestedQuantity());
        } catch (Exception ignored) {}

        errorResponse.put("timestamp", LocalDateTime.now());
        errorResponse.put("path", request.getDescription(false).replace("uri=", ""));

        return new ResponseEntity<>(errorResponse, HttpStatus.CONFLICT);
    }

    @ExceptionHandler(StockOperationException.class)
    public ResponseEntity<Map<String, Object>> handleStockOperationException(
            StockOperationException ex, WebRequest request) {

        logger.error("Error del servicio de productos: {}", ex.getMessage());

        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("success", false);
        errorResponse.put("message", "Error en operación de stock");
        errorResponse.put("error", ex.getMessage());

        // Solo agregar campos si los métodos existen
        try {
            errorResponse.put("productId", ex.getProductId());
        } catch (Exception ignored) {}

        try {
            errorResponse.put("operation", ex.getOperation());
        } catch (Exception ignored) {}

        errorResponse.put("timestamp", LocalDateTime.now());
        errorResponse.put("path", request.getDescription(false).replace("uri=", ""));

        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    //  EXCEPCIONES GENERALES DE SPRING

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleMethodArgumentNotValidException(
            MethodArgumentNotValidException ex, WebRequest request) {

        logger.warn("Error de validación: {}", ex.getMessage());

        StringBuilder message = new StringBuilder("Errores de validación: ");
        ex.getBindingResult().getFieldErrors().forEach(error -> {
            message.append(error.getField())
                    .append(" - ")
                    .append(error.getDefaultMessage())
                    .append("; ");
        });

        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("success", false);
        errorResponse.put("message", "Error de validación");
        errorResponse.put("error", message.toString());
        errorResponse.put("timestamp", LocalDateTime.now());
        errorResponse.put("path", request.getDescription(false).replace("uri=", ""));

        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String, Object>> handleHttpMessageNotReadableException(
            HttpMessageNotReadableException ex, WebRequest request) {

        logger.error("Error de formato de datos: {}", ex.getMessage());

        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("success", false);
        errorResponse.put("message", "Formato de datos inválido");
        errorResponse.put("error", "Los datos enviados no pueden ser procesados");
        errorResponse.put("timestamp", LocalDateTime.now());
        errorResponse.put("path", request.getDescription(false).replace("uri=", ""));

        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<Map<String, Object>> handleMissingServletRequestParameterException(
            MissingServletRequestParameterException ex, WebRequest request) {

        logger.error("Parámetro faltante: {}", ex.getMessage());

        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("success", false);
        errorResponse.put("message", "Parámetro requerido faltante");
        errorResponse.put("error", "Parámetro requerido faltante: " + ex.getParameterName());
        errorResponse.put("parameterName", ex.getParameterName());
        errorResponse.put("timestamp", LocalDateTime.now());
        errorResponse.put("path", request.getDescription(false).replace("uri=", ""));

        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<Map<String, Object>> handleMethodArgumentTypeMismatchException(
            MethodArgumentTypeMismatchException ex, WebRequest request) {

        logger.error("Tipo de dato inválido: {}", ex.getMessage());

        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("success", false);
        errorResponse.put("message", "Tipo de dato inválido");
        errorResponse.put("error", "Tipo de dato inválido para el parámetro: " + ex.getName());
        errorResponse.put("parameterName", ex.getName());
        errorResponse.put("timestamp", LocalDateTime.now());
        errorResponse.put("path", request.getDescription(false).replace("uri=", ""));

        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    //  EXCEPCIÓN GENÉRICA

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGenericException(
            Exception ex, WebRequest request) {

        logger.error("Error inesperado: {}", ex.getMessage(), ex);

        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("success", false);
        errorResponse.put("message", "Error interno del servidor");
        errorResponse.put("error", "Ha ocurrido un error inesperado");
        errorResponse.put("timestamp", LocalDateTime.now());
        errorResponse.put("path", request.getDescription(false).replace("uri=", ""));

        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
