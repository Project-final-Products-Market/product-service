# 📦 Product Service - Gestión de Productos

Microservicio para la gestión completa del catálogo de productos en el ecosistema Products Market.

## 📋 Descripción
El Product Service es el núcleo del inventario comercial, responsable de administrar productos, controlar stock, gestionar precios y proporcionar funcionalidades avanzadas de búsqueda para el marketplace.

## 🛠️ Stack Tecnológico

- **Java 17**
- **Spring Boot 3.1.5**
- **Spring Data JPA**
- **MySQL 8.0**
- **Spring Cloud Netflix Eureka**
- **Jakarta Validation**
- **Hibernate Validator**
- **JUnit 5 & Mockito**
- **TestContainers**

## ⚡ Funcionalidades Principales

### ✅ Gestión Completa de Productos
- Creación y actualización de productos
- Control de inventario y stock
- Eliminación segura con validaciones
- Gestión de precios con BigDecimal

### 📊 Control de Stock Inteligente
- **Reducción automática** de stock para órdenes
- **Incremento de stock** para cancelaciones
- **Verificación en tiempo real** de disponibilidad
- **Alertas de stock bajo** configurables

### 🔍 Sistema de Búsquedas Avanzadas
- **Por nombre**: Búsqueda con coincidencias parciales
- **Por rango de precio**: Filtros financieros precisos
- **Por disponibilidad**: Solo productos con stock
- **Por stock bajo**: Identificación de productos críticos

### 🛡️ Validaciones Robustas
- **Precios válidos** (mayor que cero)
- **Stock no negativo** con control estricto
- **Nombres obligatorios** (máximo 255 caracteres)
- **Descripciones opcionales** (máximo 1000 caracteres)

## 🌐 Endpoints de la API

### Operaciones CRUD Básicas

| Método | Endpoint | Descripción |
|--------|----------|-------------|
| `GET` | `/api/products` | Obtener todos los productos |
| `GET` | `/api/products/{id}` | Obtener producto por ID |
| `POST` | `/api/products` | Crear nuevo producto |
| `PUT` | `/api/products/{id}` | Actualizar producto |
| `DELETE` | `/api/products/{id}` | Eliminar producto |

### Endpoints Gateway (Puerto 8087)

| Método | Endpoint Gateway | Descripción |
|--------|------------------|-------------|
| `GET` | `http://localhost:8087/api/products` | Obtener todos los productos |
| `GET` | `http://localhost:8087/api/products/{id}` | Obtener producto por ID |
| `POST` | `http://localhost:8087/api/products` | Crear nuevo producto |
| `PUT` | `http://localhost:8087/api/products/{id}` | Actualizar producto |

### Endpoints de Búsqueda y Filtros

| Método | Endpoint Gateway | Descripción |
|--------|------------------|-------------|
| `GET` | `http://localhost:8087/api/products/search?name={name}` | Buscar por nombre |
| `GET` | `http://localhost:8087/api/products/available` | Solo productos disponibles |
| `GET` | `http://localhost:8087/api/products/price-range` | Filtrar por rango de precio |
| `GET` | `http://localhost:8087/api/products/low-stock` | Productos con stock bajo |

### Endpoints de Gestión de Stock

| Método | Endpoint Gateway | Descripción |
|--------|------------------|-------------|
| `PUT` | `http://localhost:8087/api/products/{id}/reduce-stock` | Reducir stock (para órdenes) |
| `PUT` | `http://localhost:8087/api/products/{id}/increase-stock` | Aumentar stock (cancelaciones) |
| `GET` | `http://localhost:8087/api/products/{id}/check-stock` | Verificar stock disponible |

### Endpoints de Estadísticas

| Método | Endpoint Gateway | Descripción |
|--------|------------------|-------------|
| `GET` | `http://localhost:8087/api/products/stats/total` | Total de productos |
| `GET` | `http://localhost:8087/api/products/stats/available` | Productos disponibles |

**Parámetros comunes:**
- `name`: Nombre a buscar (coincidencias parciales)
- `minPrice`, `maxPrice`: Rango de precios (formato decimal)
- `threshold`: Límite para stock bajo (por defecto: 10)
- `quantity`: Cantidad para operaciones de stock

**Respuesta de estadísticas incluye:**
- Conteo total de productos
- Conteo de productos con stock disponible
- Timestamp de la consulta

## 📋 Modelo de Datos

```java
@Entity
public class Product {
    private Long id;                    // ID único autogenerado
    private String name;                // Nombre (obligatorio, máx 255 chars)
    private String description;         // Descripción (opcional, máx 1000 chars)
    private BigDecimal price;           // Precio (obligatorio, > 0)
    private Integer stock;              // Stock (obligatorio, >= 0)
    private LocalDateTime createdAt;    // Fecha de creación
    private LocalDateTime updatedAt;    // Última modificación
}
```

## ⚙️ Configuración del Servicio

```properties
server.port=8082
spring.datasource.url=jdbc:mysql://localhost:3306/marketjosemsp
spring.datasource.username=root
spring.datasource.password=****
eureka.client.service-url.defaultZone=http://localhost:8761/eureka/
```

## 🚀 Orden de Ejecución

1. **Iniciar MySQL** en puerto 3306
2. **Iniciar Eureka Server** en puerto 8761
3. **Iniciar API Gateway** en puerto 8087
4. **Ejecutar Product Service** en puerto 8082
5. **Iniciar User Service** en puerto 8081 (opcional)
6. **Iniciar Order Service** en puerto 8083 (opcional)

### Verificar funcionamiento:
- **Directo**: [http://localhost:8082/api/products](http://localhost:8082/api/products)
- **Gateway**: [http://localhost:8087/api/products](http://localhost:8087/api/products)

## 🔄 Comunicación entre Servicios

### 🛒 Order Service Integration
- Reducción automática de stock en órdenes
- Verificación de disponibilidad antes de venta
- Restauración de stock en cancelaciones

### 🌐 API Gateway Integration
- Enrutamiento inteligente de peticiones
- Balanceador de carga automático
- Logging centralizado de operaciones

## 🧠 Lógica de Negocio

### Sistema de Validaciones Automáticas
```java
// Validación de precio
if (price <= 0) → ProductValidationException

// Control de stock
if (stock < 0) → ProductValidationException

// Verificación de stock suficiente
if (requestedQuantity > availableStock) → InsufficientStockException
```

### Operaciones de Stock Transaccionales
- **Reducción de stock**: Atomica y con rollback automático
- **Incremento de stock**: Validación de cantidad positiva
- **Verificación de disponibilidad**: Consulta optimizada en base de datos

### Gestión de Precios
- Precisión decimal con BigDecimal
- Validación automática de rangos
- Soporte para filtros de precio avanzados

## 🚨 Manejo de Errores

### Casos Críticos Manejados
- **Producto no encontrado** (404)
- **Stock insuficiente** (409 Conflict)
- **Datos de validación inválidos** (400 Bad Request)
- **Operaciones de stock fallidas** (500 Internal Server Error)

### Excepciones Específicas
- **ProductNotFoundException**: Producto inexistente
- **InsufficientStockException**: Stock insuficiente para operación
- **ProductValidationException**: Datos inválidos
- **StockOperationException**: Error en operaciones de stock

### Estructura de Respuestas
- **Éxito**: Datos completos + metadata + timestamps
- **Error**: Código específico + mensaje descriptivo + detalles adicionales
- **Stock insuficiente**: Stock disponible + cantidad solicitada

## 🛒 Casos de Uso del Marketplace

### Gestión de Inventario
- Creación de catálogo de productos
- Control de stock en tiempo real
- Gestión de precios dinámicos

### Operaciones Comerciales
- Verificación de disponibilidad para órdenes
- Reducción automática de stock en ventas
- Restauración de stock en cancelaciones

### Análisis y Reportes
- Identificación de productos con stock bajo
- Estadísticas de inventario
- Filtros avanzados para análisis de precios

### Búsquedas de Clientes
- Búsqueda por nombre de producto
- Filtros por rango de precio
- Visualización solo de productos disponibles

---

## 👨‍ Autor

### **Jose Manuel Siguero Pérez**
### [Linkedin](https://www.linkedin.com/in/jose-manuel-siguero)

----
**Parte del Sistema de Microservicios Products Market**