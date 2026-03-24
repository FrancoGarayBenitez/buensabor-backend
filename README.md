# 🍔 El Buen Sabor — Backend API

Sistema de gestión integral para un negocio gastronómico, desarrollado como API REST con **Spring Boot 3.4**. Abarca todo el ciclo operativo: desde la gestión de productos e insumos, hasta el procesamiento de pedidos, pagos con MercadoPago y notificaciones en tiempo real vía WebSocket.

---

## 🧰 Stack Tecnológico

| Categoría         | Tecnología                               |
| ----------------- | ---------------------------------------- |
| **Lenguaje**      | Java 21                                  |
| **Framework**     | Spring Boot 3.4.5                        |
| **Seguridad**     | Spring Security + JWT (JJWT 0.12.5)      |
| **Base de datos** | MySQL 8 + Spring Data JPA / Hibernate    |
| **Mapeo de DTOs** | MapStruct 1.5.5                          |
| **Tiempo real**   | WebSocket (STOMP + SockJS)               |
| **Pagos**         | MercadoPago SDK 2.1.28                   |
| **Email**         | Spring Mail (SMTP Gmail)                 |
| **PDF**           | iText 7 + html2pdf                       |
| **Build**         | Gradle                                   |
| **Otros**         | Lombok, Bean Validation, Spring DevTools |

---

## 🏗️ Arquitectura

El proyecto sigue una **arquitectura en capas** con separación clara de responsabilidades:

```
src/main/java/com/elbuensabor/
├── config/              # Configuración (Security, CORS, WebSocket, JWT Filter, DataSeeder)
├── controllers/         # Controladores REST (16 controllers)
├── dto/
│   ├── request/         # DTOs de entrada con validaciones
│   └── response/        # DTOs de salida (sin exponer entidades)
├── entities/            # Entidades JPA con lógica de dominio
├── exceptions/          # Manejo global de excepciones (@ControllerAdvice)
├── repository/          # Repositorios Spring Data JPA
└── services/
    ├── impl/            # Implementaciones de servicios (incluye JwtService, WebSocket, Email)
    └── mapper/          # Mappers MapStruct (17 mappers)
```

---

## 🔐 Seguridad y Autenticación

- **Autenticación stateless** con JWT (HS256), sin sesiones del lado del servidor.
- **5 roles con control de acceso granular**: `ADMIN`, `CAJERO`, `COCINERO`, `DELIVERY`, `CLIENTE`.
- Autorización a nivel de endpoint (`SecurityFilterChain`) y a nivel de método (`@PreAuthorize`).
- Contraseñas hasheadas con **BCrypt**.
- Flujo de **restablecimiento de contraseña** por email con token JWT de corta duración.
- `DataSeeder` para creación automática de usuario admin y datos iniciales al arrancar.

---

## 📦 Modelo de Dominio (Entidades principales)

El sistema modela un negocio gastronómico con **+25 entidades JPA** relacionadas:

- **Artículos**: Herencia `Articulo` → `ArticuloInsumo` (ingredientes con stock) / `ArticuloManufacturado` (productos elaborados con receta y costo de producción).
- **Categorías**: Estructura jerárquica con autorrelación padre-hijo y tipo (`INSUMO` / `MANUFACTURADO`).
- **Pedidos**: Ciclo de vida completo con estados (`PENDIENTE` → `PREPARACION` → `LISTO` → `ENTREGADO` / `CANCELADO`), tipo de envío, forma de pago, tiempo estimado, observaciones.
- **Facturación y Pagos**: `Factura` con detalles de subtotal/descuento/envío, `Pago` con integración a MercadoPago (`DatosMercadoPago`).
- **Promociones**: Sistema flexible con rango de fechas/horarios, tipos de descuento (porcentual/fijo), detalle multi-artículo.
- **Usuarios y Clientes**: `Usuario` implementa `UserDetails` de Spring Security. `Cliente` con domicilios múltiples e imagen de perfil.
- **Gestión de stock**: Control de stock con estado (CRITICO/BAJO/OK), histórico de precios por compra de insumos.
- **Geografía**: `Domicilio` → `Localidad` → `Provincia` → `País`.

---

## 🔄 Notificaciones en Tiempo Real (WebSocket)

Implementación de **WebSocket con STOMP sobre SockJS** para notificaciones push:

| Canal   | Destino                             | Descripción                              |
| ------- | ----------------------------------- | ---------------------------------------- |
| Cocina  | `/topic/cocina/nuevos`              | Nuevos pedidos entrantes                 |
| Cocina  | `/topic/cocina/cancelaciones`       | Pedidos cancelados (detener preparación) |
| Cajero  | `/topic/cajero/pedidos`             | Nuevos pedidos y cambios de estado       |
| Cliente | `/user/{email}/queue/pedido/estado` | Estado de su pedido (personalizado)      |
| General | `/topic/pedidos/estados`            | Broadcast de cambios de estado           |

---

## 💳 Integración de Pagos

- **MercadoPago SDK**: Creación de preferencias de pago y seguimiento de transacciones (`paymentId`, `status`, `dateApproved`).
- **Efectivo**: Confirmación manual por cajero con trazabilidad (`usuarioConfirmaPago`, `fechaConfirmacionPago`).
- **Facturación automática**: Generación de factura y exportación a PDF con iText 7.

---

## 🛍️ API REST — Endpoints Principales

### Autenticación (`/api/auth`)

- `POST /register` — Registro de cliente (con domicilio e imagen)
- `POST /login` — Login con JWT
- `POST /forgot-password` — Solicitud de restablecimiento por email
- `POST /reset-password` — Restablecimiento de contraseña

### Catálogo Público (`/api/catalogo`) — Sin autenticación

- `GET /articulos` — Listado de productos disponibles
- `GET /articulos/{id}` — Detalle de producto
- `GET /promociones` — Promociones vigentes

### Pedidos (`/api/pedidos`) — Por rol

- `POST /` — Crear pedido (CLIENTE)
- `GET /mis-pedidos` — Historial del cliente
- `GET /del-dia` — Pedidos del día (CAJERO/ADMIN)
- `GET /cocina` — Cola de cocina (COCINERO)
- `GET /delivery` — Pedidos asignados (DELIVERY)
- `PUT /cambiar-estado` — Transición de estado
- `PUT /{id}/iniciar-preparacion` — Iniciar cocción
- `PUT /{id}/marcar-listo` — Marcar como listo
- `PUT /{id}/marcar-entregado` — Confirmar entrega
- `PUT /cancelar` — Cancelar pedido (multi-rol)
- `PUT /asignar-delivery` — Asignar repartidor
- `PUT /extender-tiempo` — Extender tiempo estimado
- `POST /confirmar-pago` — Confirmar pago efectivo (CAJERO)

### Gestión Administrativa (ADMIN)

- `/api/articulos-insumo` — CRUD de insumos con control de stock
- `/api/articulos-manufacturados` — CRUD de productos elaborados
- `/api/categorias` — Gestión jerárquica de categorías
- `/api/promociones` — CRUD de promociones
- `/api/empleados` — Gestión de empleados
- `/api/usuarios` — Gestión de usuarios y roles
- `/api/compras-insumo` — Registro de compras con histórico de precios
- `/api/imagenes` — Upload y gestión de imágenes

---

## ⚙️ Patrones y Buenas Prácticas Aplicadas

- **Patrón DTO**: Separación completa entre entidades y capa de presentación, con DTOs de request y response diferenciados.
- **MapStruct**: Mapeo automático de entidades ↔ DTOs sin boilerplate manual (17 mappers).
- **Service Layer con interfaces**: Todas las operaciones detrás de interfaces (`IService`) con implementaciones concretas.
- **Generic Service**: Servicio base genérico (`GenericServiceImpl`) para operaciones CRUD comunes.
- **Global Exception Handler**: Manejo centralizado de excepciones con respuestas estandarizadas (`@ControllerAdvice`).
- **Lógica de dominio en entidades**: Métodos como `verificarStockSuficiente()`, `actualizarCostoProduccion()`, `estaVigente()` encapsulados en las entidades.
- **Soft delete**: Eliminación lógica (`eliminado = true`) en artículos y promociones.
- **Auditoría de acciones**: Trazabilidad de quién cancela, confirma pagos, timestamps de cada etapa del pedido.

---

## 🚀 Cómo Ejecutar

### Prerrequisitos

- Java 21
- MySQL 8+
- Gradle

### Configuración

1. Copiar el archivo de configuración de ejemplo:
   ```bash
   cp src/main/resources/application.properties.example src/main/resources/application.properties
   ```
2. Editar `application.properties` con tus credenciales (ver indicaciones dentro del archivo).
3. Crear la base de datos MySQL (se crea automáticamente si no existe):
   ```sql
   CREATE DATABASE IF NOT EXISTS el_buen_sabor;
   ```
4. Ejecutar:
   ```bash
   ./gradlew bootRun
   ```
5. Al iniciar, el `DataSeeder` crea automáticamente:
   - Usuario admin: `admin@elbuensabor.com` / `Admin123!`
   - Unidades de medida predefinidas (g, ml, unidad, docena, porción, etc.)

---

## 📊 Alcance del Proyecto

| Métrica                   | Valor |
| ------------------------- | ----- |
| Entidades JPA             | +25   |
| Controladores REST        | 16    |
| Servicios                 | 18    |
| Mappers MapStruct         | 17    |
| DTOs (Request + Response) | +30   |
| Roles de usuario          | 5     |
| Estados de pedido         | 5     |

---

## 👤 Autor

**Franco Garay**

> Proyecto desarrollado como sistema integral de gestión gastronómica, demostrando dominio en desarrollo backend con Spring Boot, seguridad JWT, integración de pagos, comunicación en tiempo real y diseño de APIs RESTful profesionales.
