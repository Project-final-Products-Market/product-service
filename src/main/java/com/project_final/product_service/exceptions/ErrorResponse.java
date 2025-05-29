package com.project_final.product_service.exceptions;

import java.time.LocalDateTime;

/**
 * Clase para estructurar las respuestas de error del servicio de productos
 */
public class ErrorResponse {

    private String errorCode;
    private String message;
    private String details;
    private LocalDateTime timestamp;
    private String path;
    private int status;
    private Object additionalInfo; // Para información extra como stock disponible

    public ErrorResponse() {
        this.timestamp = LocalDateTime.now();
    }

    public ErrorResponse(String errorCode, String message) {
        this();
        this.errorCode = errorCode;
        this.message = message;
    }

    public ErrorResponse(String errorCode, String message, String details, String path, int status) {
        this();
        this.errorCode = errorCode;
        this.message = message;
        this.details = details;
        this.path = path;
        this.status = status;
    }

    // Getters y Setters
    public String getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public Object getAdditionalInfo() {
        return additionalInfo;
    }

    public void setAdditionalInfo(Object additionalInfo) {
        this.additionalInfo = additionalInfo;
    }
}
