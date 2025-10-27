# El Buen Sabor - API REST

Una API REST completa para la gestión de un restaurante/negocio gastronómico. Desarrollada con Spring Boot, incluye gestión de productos, pedidos, clientes, pagos con MercadoPago y más.

## 🚀 Características Principales

- **Gestión de Productos**: Artículos manufacturados e insumos con control de stock
- **Sistema de Pedidos**: Gestión completa del flujo de pedidos (PENDIENTE → PREPARACION → LISTO → ENTREGADO)
- **Gestión de Clientes**: Registro, autenticación y manejo de domicilios
- **Integración con MercadoPago**: Procesamiento de pagos online
- **Control de Stock**: Seguimiento automático de ingredientes y productos
- **Sistema de Categorías**: Organización jerárquica de productos
- **Autenticación JWT**: Seguridad basada en tokens
- **Arquitectura Modular**: Implementación con DTOs, Mappers y Services

## 🛠️ Tecnologías Utilizadas

- **Backend**: Spring Boot 3.x
- **Base de Datos**: MySQL 8
- **Seguridad**: Spring Security + JWT
- **Mapeo**: MapStruct
- **Validación**: Bean Validation
- **Documentación**: Spring Boot DevTools
- **Pagos**: MercadoPago SDK
- **ORM**: Hibernate/JPA

## 📋 Requisitos Previos

- Java 17 o superior
- MySQL 8.0+
- Maven 3.6+
- Cuenta de MercadoPago (para pagos)

## ⚙️ Configuración

### 1. Base de Datos

```sql
CREATE DATABASE el_buen_sabor;
```

### 2. Variables de Entorno

Configurar en `application.properties`:

```properties
# Base de Datos
spring.datasource.url=jdbc:mysql://localhost:3306/el_buen_sabor
spring.datasource.username=tu_usuario
spring.datasource.password=tu_password

# JWT
jwt.secret=tu_clave_secreta_super_segura
jwt.expiration=86400

# MercadoPago
mercadopago.access.token=TEST-tu-access-token
mercadopago.public.key=TEST-tu-public-key
```

### 3. Instalación

```bash
# Clonar el repositorio
git clone https://github.com/tu-usuario/el-buen-sabor.git
cd el-buen-sabor

# Compilar el proyecto
mvn clean compile

# Ejecutar la aplicación
mvn spring-boot:run
```

La aplicación estará disponible en: `http://localhost:8080`

## 📚 Estructura del Proyecto

```
src/main/java/com/elbuensabor/
├── config/                 # Configuraciones (CORS, Security, JWT, MercadoPago)
├── controllers/            # Controladores REST
├── dto/                   # Data Transfer Objects
│   ├── request/           # DTOs para requests
│   └── response/          # DTOs para responses
├── entities/              # Entidades JPA
├── exceptions/            # Manejo de excepciones
├── repository/            # Repositorios JPA
└── services/              # Lógica de negocio
    ├── impl/              # Implementaciones
    └── mapper/            # Mappers de MapStruct
```

## 🔄 Endpoints Principales

### Autenticación
- `POST /api/auth/login` - Iniciar sesión
- `GET /api/auth/validate` - Validar token
- `GET /api/auth/me` - Obtener usuario actual

### Clientes
- `POST /api/clientes/register` - Registrar cliente
- `GET /api/clientes` - Listar clientes
- `GET /api/clientes/{id}` - Obtener cliente por ID

### Productos
- `GET /api/articulos-manufacturados` - Listar productos manufacturados
- `POST /api/articulos-manufacturados` - Crear producto
- `GET /api/articulos-insumo` - Listar insumos
- `GET /api/articulos-insumo/stock/critico` - Stock crítico

### Pedidos
- `POST /api/pedidos` - Crear pedido
- `GET /api/pedidos` - Listar todos los pedidos
- `PUT /api/pedidos/{id}/confirmar` - Confirmar pedido
- `PUT /api/pedidos/{id}/preparacion` - Marcar en preparación
- `PUT /api/pedidos/{id}/listo` - Marcar como listo
- `PUT /api/pedidos/{id}/entregado` - Marcar como entregado

### Pagos
- `POST /api/pagos` - Crear pago
- `POST /api/pagos/{id}/crear-preferencia-mp` - Crear preferencia MercadoPago
- `GET /api/pagos/factura/{facturaId}` - Pagos por factura

### Categorías
- `GET /api/categorias` - Listar categorías
- `POST /api/categorias` - Crear categoría
- `GET /api/categorias/principales` - Categorías principales

## 🏗️ Modelo de Datos

### Entidades Principales

- **Cliente**: Información del cliente y usuario
- **Pedido**: Pedidos con estados y detalles
- **Articulo**: Clase base para productos
  - **ArticuloManufacturado**: Productos elaborados con recetas
  - **ArticuloInsumo**: Ingredientes y productos simples
- **Categoria**: Organización jerárquica de productos
- **Pago**: Gestión de pagos múltiples por factura
- **Factura**: Documentos de venta

### Estados del Pedido

```
PENDIENTE → PREPARACION → LISTO → ENTREGADO
     ↓
  CANCELADO
```

## 💳 Integración con MercadoPago

### Configuración
```properties
mercadopago.access.token=TEST-tu-token
mercadopago.sandbox.mode=true
mercadopago.success.url=http://localhost:8080/payment/success
mercadopago.failure.url=http://localhost:8080/payment/failure
```

### Flujo de Pago
1. Cliente crea un pedido
2. Se genera una factura
3. Se crea una preferencia en MercadoPago
4. Cliente completa el pago
5. Webhook actualiza el estado del pago

## 🔒 Seguridad

- **Autenticación**: JWT tokens
- **Autorización**: Roles de usuario (CLIENTE, ADMIN, COCINERO, etc.)
- **CORS**: Configurado para desarrollo local
- **Validación**: Bean Validation en todos los DTOs

## 🧪 Testing

```bash
# Ejecutar tests
mvn test

# Tests con cobertura
mvn test jacoco:report
```

## 📦 Deployment

### Desarrollo
```bash
mvn spring-boot:run
```

### Producción
```bash
mvn clean package
java -jar target/elbuensabor-0.0.1-SNAPSHOT.jar
```

## 🔧 Configuración para Producción

1. **Base de Datos**: Configurar conexión a BD de producción
2. **MercadoPago**: Cambiar a tokens de producción
3. **JWT**: Usar secret más seguro
4. **CORS**: Configurar dominios permitidos
5. **SSL**: Habilitar HTTPS

```properties
# Producción
mercadopago.access.token=APP_USR-tu-token-produccion
mercadopago.sandbox.mode=false
jwt.secret=clave-super-segura-de-produccion
```

## 📝 Funcionalidades Destacadas

### Control de Stock Inteligente
- Seguimiento automático de ingredientes
- Alertas de stock crítico y bajo
- Validación de disponibilidad antes de confirmar pedidos

### Gestión de Recetas
- Productos manufacturados con listas de ingredientes
- Cálculo automático de costos
- Gestión de márgenes de ganancia

### Sistema de Pagos Flexible
- Múltiples pagos por factura
- Estados de pago detallados
- Integración completa con MercadoPago

### Arquitectura Limpia
- Separación clara de responsabilidades
- DTOs para requests y responses
- Mappers automáticos con MapStruct
- Manejo centralizado de excepciones

## 🐛 Troubleshooting

### Problemas Comunes

1. **Error de conexión a BD**
   ```
   Verificar credenciales en application.properties
   Asegurar que MySQL esté ejecutándose
   ```

2. **Error de MercadoPago**
   ```
   Verificar tokens de acceso
   Confirmar configuración de sandbox/producción
   ```

3. **Error de JWT**
   ```
   Verificar que el secret esté configurado
   Comprobar expiración del token
   ```

## 🤝 Contribución

1. Fork el proyecto
2. Crear una rama para tu feature (`git checkout -b feature/nueva-funcionalidad`)
3. Commit tus cambios (`git commit -am 'Agregar nueva funcionalidad'`)
4. Push a la rama (`git push origin feature/nueva-funcionalidad`)
5. Crear un Pull Request

## 🙏 Agradecimientos

- Spring Boot team por el framework
- MercadoPago por la API de pagos
- MapStruct por el mapeo automático
- Comunidad de desarrolladores Java/Spring

👥 Equipo de Desarrollo
Este proyecto está siendo desarrollado por:

- Franco Garay - @FrancoGarayBenitez
- Luciano Reggio - @LucianoReggio
- Octavio Ragusa - @Octavio1993
- Matias Picón - @Pykon26
- Pedro Giorlando - @PedroGiorlando

📞 Contacto
Para consultas sobre el proyecto, puedes contactar a cualquier miembro del equipo de desarrollo a través de sus perfiles de GitHub.

---

**El Buen Sabor** 🍕
