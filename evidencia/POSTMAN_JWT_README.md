# 🔐 Colección Postman - Library Management System con JWT

## 📋 Descripción

Colección completa de Postman con **autenticación JWT automática** usando Keycloak para el Sistema de Gestión de Biblioteca.

## ✨ Características

✅ **Autenticación JWT Manual** - Obtén el token una vez y úsalo en todas las peticiones  
✅ **Token guardado en variables** - Se mantiene disponible para todos los endpoints  
✅ **Variables pre-configuradas** para Keycloak  
✅ **Endpoints protegidos** con @PreAuthorize y Spring Security  

## 🚀 Instalación

### 1. Importar la colección en Postman

1. Abrir **Postman**
2. Click en **Import**
3. Seleccionar el archivo: `Library-Management-API.postman_collection.json`
4. Click en **Import**

### 2. Obtener Token JWT (Obligatorio)

**IMPORTANTE:** Antes de probar cualquier endpoint, debes obtener el token JWT:

1. En Postman, navegar a la carpeta: **Keycloak - Authentication**
2. Ejecutar la petición: **Get Access Token (Manual)**
3. Verificar que la respuesta tenga status **200 OK**
4. El token se guardará automáticamente en la variable `{{access_token}}`

✅ **Ahora puedes usar cualquier endpoint** - El token se enviará automáticamente en el header `Authorization: Bearer {token}`

### 3. Verificar que Keycloak esté corriendo

Antes de obtener el token, asegúrate de que Keycloak esté disponible:

```powershell
# Verificar si Keycloak responde
curl http://localhost:8180
```

Si no está corriendo, inicia Keycloak desde tu proyecto.

### 4. Configurar variables (solo si es necesario)

Las variables ya vienen pre-configuradas, pero puedes modificarlas si tu configuración es diferente:

| Variable | Valor por Defecto | Descripción |
|----------|-------------------|-------------|
| `gateway_url` | http://localhost:8090 | URL del API Gateway |
| `ms_book_url` | http://localhost:8082 | URL directa del MS-Book (dev) |
| `keycloak_url` | http://localhost:8180 | URL de Keycloak |
| `keycloak_realm` | library-realm | Realm de Keycloak |
| `keycloak_client_id` | library-client | Client ID de Keycloak |
| `keycloak_username` | admin | Usuario de Keycloak |
| `keycloak_password` | admin | Contraseña del usuario |

**Para modificar variables:**
1. Click derecho en la colección
2. **Edit** > **Variables**
3. Modificar los valores necesarios
4. **Save**

## 🔑 Cómo Funciona la Autenticación

### Flujo Manual (Recomendado)

```
1. Ejecutar "Get Access Token (Manual)" en carpeta Keycloak
   ↓
2. El token JWT se obtiene de Keycloak
   ↓
3. Se guarda automáticamente en {{access_token}}
   ↓
4. Todas las demás peticiones usan este token:
   Authorization: Bearer {{access_token}}
   ↓
5. Si el token expira (después de 5 minutos):
   - Volver a ejecutar "Get Access Token (Manual)"
```

### ¿Por qué Manual?

El script automático puede causar problemas de rendimiento en Postman:
- ❌ Se queda "cargando" indefinidamente
- ❌ Bloquea la interfaz con "Running Scripts"
- ❌ Hace peticiones adicionales innecesarias

✅ **Método Manual es más confiable:**
- Solo obtienes el token cuando lo necesitas
- Más rápido y sin bloqueos
- Más control sobre el proceso

### Script Pre-Request (Deshabilitado)

```javascript
// ⚠️ DESHABILITADO para evitar bloqueos
// Usa el método manual en su lugar
```

### Script Test (Automático)

```javascript
// Este script se ejecuta DESPUÉS de cada petición
// Maneja errores 401/403
// Muestra logs en la consola de Postman
```

## 📝 Guía de Uso Rápida

### Paso 1: Obtener Token

1. Ir a: **Keycloak - Authentication** > **Get Access Token (Manual)**
2. Click en **Send**
3. Verificar respuesta 200 OK
4. ✅ Token guardado en `{{access_token}}`

### Paso 2: Probar Endpoints

Ahora puedes ejecutar cualquier endpoint, por ejemplo:

1. Ir a: **Books - CRUD Operations** > **GET All Books**
2. Click en **Send**
3. ✅ La petición incluye automáticamente: `Authorization: Bearer {token}`

### Paso 3: Renovar Token (cada 5 minutos)

Cuando el token expire (después de ~5 minutos):

1. Si recibes error **401 Unauthorized**
2. Volver a: **Get Access Token (Manual)**
3. Click en **Send**
4. ✅ Continuar usando los endpoints

## 📂 Estructura de la Colección

### 1. **Gateway - Health & Info** (3 peticiones)
- Gateway Health
- Gateway Info
- Gateway Routes

### 2. **Books - CRUD Operations** (6 peticiones)
- ✅ GET All Books (Paginated)
- ✅ GET All Books (No Pagination)
- ✅ GET Book by ID
- ✅ POST Create Book
- ✅ PUT Update Book
- ✅ DELETE Book

### 3. **Books - Search Operations** (7 peticiones)
- Search by Title
- Search by Author
- Search by Genre
- Search by Pages Range
- Full Text Search
- Advanced Search
- Advanced Search Paginated

### 4. **Books - Filters & Analytics** (4 peticiones)
- Get Available Books
- Get All Authors
- Get Statistics by Genre
- Get Popular Books

### 5. **Books - Business Operations** (2 peticiones)
- Borrow Book
- Return Book

### 6. **Direct MS-Book Access** (4 peticiones)
Acceso directo al microservicio (solo desarrollo)

### 7. **Keycloak - Authentication** (4 peticiones) 🔐
- **Get Access Token (Manual)** ⭐ **USAR PRIMERO**
- Refresh Token
- Logout
- Test Endpoint Protected

## 🧪 Pruebas Paso a Paso

### Primera Vez Usando la Colección

**Paso 1:** Verificar que Keycloak está corriendo
```powershell
# Abrir navegador en:
http://localhost:8180
```

**Paso 2:** Obtener Token JWT
1. En Postman, ir a: `Keycloak - Authentication`
2. Ejecutar: `Get Access Token (Manual)`
3. Verificar respuesta 200 OK
4. Ver en la consola: "Token guardado exitosamente"

**Paso 3:** Probar un endpoint
1. Ir a: `Books - CRUD Operations` > `GET All Books`
2. Click en **Send**
3. Verificar respuesta 200 OK con lista de libros

**Paso 3b (Opcional):** Probar Config Server
1. Ir a: `Config Server - Endpoints` > `Get config via Gateway (ms-book/default)`
2. Click en **Send**
3. Ver respuesta JSON con propiedades para `ms-book` perfil `default`

**Paso 4:** Verificar autenticación
1. Ver en **Headers** de la petición:
   ```
   Authorization: Bearer eyJhbGciOiJSUzI1NiIsInR5cCI...
   ```

✅ **¡Listo!** Ahora puedes usar todos los endpoints.

### Verificar Endpoints Protegidos

1. **Test sin Token:**
   - Ir a: `Keycloak - Authentication` > `Test Endpoint Protected`
   - Esta petición NO tiene token
   - Ejecutar la petición
   - Debe devolver: **401 Unauthorized**

2. **Test con Token:**
   - Primero ejecutar: `Get Access Token (Manual)`
   - Luego ejecutar cualquier endpoint de `Books - CRUD Operations`
   - Debe devolver: **200 OK** (o código exitoso)

### Ver el Contenido del Token JWT

1. Ir a: `Keycloak - Authentication` > `Get Access Token (Manual)`
2. Ejecutar la petición
3. Copiar el valor de `access_token` de la respuesta
4. Ir a https://jwt.io
5. Pegar el token en "Encoded"
6. Ver información decodificada:
   ```json
   {
     "sub": "usuario-id",
     "realm_access": {
       "roles": ["ADMIN", "USER"]
     },
     "exp": 1234567890
   }
   ```

## 👥 Cambiar Usuario

### Opción 1: Modificar Variables de Colección

1. Click derecho en la colección
2. **Edit** > **Variables**
3. Modificar:
   - `keycloak_username` (ejemplo: cambiar de "admin" a "user")
   - `keycloak_password` (ejemplo: cambiar a "user123")
4. **Save**
5. Ejecutar nuevamente: `Get Access Token (Manual)`
6. ✅ Ahora usarás el nuevo usuario

### Opción 2: Usuarios de Ejemplo

Puedes crear estas configuraciones según tus usuarios en Keycloak:

**Usuario Administrador:**
```
username: admin
password: admin
roles: ADMIN, USER
```

**Usuario Regular:**
```
username: user
password: user123
roles: USER
```

**Usuario Empleado:**
```
username: empleado
password: empleado123
roles: EMPLOYEE
```

## 📊 Ver Logs de Autenticación

### Activar Consola de Postman

1. En Postman, ir a: **View** > **Show Postman Console** (Alt+Ctrl+C)
2. La consola se abrirá en la parte inferior

### Logs al Obtener Token

Cuando ejecutes `Get Access Token (Manual)`, verás:

```
✅ Token JWT obtenido exitosamente
Token guardado en variable: access_token
Expira en: 300 segundos (5 minutos)
```

### Logs en Peticiones

Al ejecutar cualquier endpoint, verás:

```
Pre-request: Usando token manual desde variables
✅ Petición exitosa - GET All Books (paginado)
Status: 200 OK
```

### Logs de Errores

Si el token expiró o es inválido:

```
❌ Error 401 Unauthorized - Token inválido o expirado
Solución: Ejecutar nuevamente 'Get Access Token (Manual)'
```

## ❌ Solución de Problemas

### Problema: "Postman se queda cargando con 'Running Scripts'"

**Causa:** El script automático de pre-request causaba bloqueos

**Solución:** ✅ **YA ESTÁ RESUELTO**
- El script automático ha sido **deshabilitado**
- Ahora usas el método **manual** que es más rápido
- Solo ejecuta `Get Access Token (Manual)` cuando lo necesites

### Problema: "No se pudo obtener el token JWT"

**Causa:** Keycloak no está disponible o credenciales incorrectas

**Solución:**
1. Verificar que Keycloak esté corriendo:
   ```powershell
   # Abrir en navegador:
   http://localhost:8180
   ```
2. Verificar variables de colección (click derecho > Edit > Variables):
   - `keycloak_url` → http://localhost:8180
   - `keycloak_realm` → library-realm
   - `keycloak_client_id` → library-client
   - `keycloak_username` → admin
   - `keycloak_password` → admin

### Problema: 401 Unauthorized

**Causa:** Token inválido, expirado o no existe

**Solución:**
1. Ir a: `Keycloak - Authentication` > `Get Access Token (Manual)`
2. Ejecutar la petición
3. Verificar respuesta 200 OK
4. ✅ Token renovado, reintentar el endpoint

### Problema: 403 Forbidden

**Causa:** El usuario no tiene permisos para la operación

**Ejemplo:**
- Usuario "user" intenta DELETE (requiere rol ADMIN)
- Resultado: 403 Forbidden

**Solución:**
1. Verificar el rol del usuario en Keycloak
2. Verificar que el endpoint requiere permisos específicos:
   ```java
   @PreAuthorize("hasRole('ADMIN')")  // Solo ADMIN
   @DeleteMapping("/{id}")
   ```
3. Cambiar a un usuario con permisos adecuados:
   - Modificar variables: `keycloak_username` y `keycloak_password`
   - Ejecutar: `Get Access Token (Manual)`
   - Reintentar la operación

### Problema: Token no se está usando en las peticiones

**Causa:** La autenticación de la colección no está configurada

**Solución:**
1. Click derecho en la colección: **Library Management System**
2. **Edit** > **Authorization**
3. Verificar configuración:
   - **Type:** Bearer Token
   - **Token:** `{{access_token}}`
4. **Save**
5. Verificar que "Inherit auth from parent" esté habilitado en cada petición

### Problema: "Cannot GET /api/books - 404 Not Found"
### Problema: `{"error":"unauthorized_client","error_description":"Invalid client or Invalid client credentials"}`

**Causas posibles:**
1. El `client_id` es incorrecto.
2. El cliente en Keycloak está INACTIVO o borrado.
3. El cliente es de tipo CONFIDENTIAL y falta `client_secret`.
4. No está habilitado "Direct Access Grants" (password grant) en el cliente.
5. El `realm` usado en la URL no coincide con el realm real.
6. Se está usando un `client_secret` equivocado.

**Verificación en Keycloak (pasos):**
1. Ingresar a consola admin: `http://localhost:8180` (Keycloak UI).
2. Seleccionar el realm: `library-realm` (o el correcto en tu instalación).
3. Ir a: Clients → Buscar `library-client`.
4. Revisar:
    - Enabled: ON.
    - Access Type: PUBLIC (si usarás la petición "Get Access Token (Manual)").
    - Si es CONFIDENTIAL: copiar el `Client Secret` y ponerlo en variable `keycloak_client_secret`.
    - Direct Access Grants Enabled: ON.
5. Guardar cambios.

**Si cliente es PUBLIC:** Usa petición: `Get Access Token (Manual)`.

**Si cliente es CONFIDENTIAL:**
1. Editar variable `keycloak_client_secret` y colocar el secreto.
2. Usar petición: `Get Access Token (Confidential Client)`.

**Ejemplo petición (CONFIDENTIAL):**
```
grant_type=password
client_id=library-client
client_secret=XXXXXXXXXXXX
username=admin
password=admin
```

**Checklist rápido:**
- [ ] Realm correcto
- [ ] client_id correcto
- [ ] Direct Access Grants habilitado
- [ ] Tipo PUBLIC sin secret, o CONFIDENTIAL con secret
- [ ] Usuario y contraseña válidos

**Resultado esperado (200 OK):**
```json
{
   "access_token": "eyJhbGciOi...",
   "expires_in": 300,
   "token_type": "Bearer"
}
```

Si persiste el error, prueba regenerar el client secret (CONFIDENTIAL) o crear un cliente nuevo PUBLIC para descartar configuración corrupta.


**Causa:** Gateway no está corriendo

**Solución:**
```powershell
# Verificar que el Gateway esté en puerto 8090:
curl http://localhost:8090/gateway/health

# Verificar que el Config Server responde:
curl {{config_server_url}}/ms-book/default
```

## 🔐 Configuración de Seguridad en el Backend

Los endpoints están protegidos con:

### Spring Security

```java
@Configuration
@EnableWebSecurity
public class SecurityConfig {
    // Configuración de seguridad
}
```

### @PreAuthorize en Controladores

```java
@PreAuthorize("hasRole('ADMIN')")
@DeleteMapping("/{id}")
public ResponseEntity<Void> deleteBook(@PathVariable Long id) {
    // Solo usuarios con rol ADMIN
}

@PreAuthorize("hasAnyRole('USER', 'ADMIN')")
@GetMapping
public ResponseEntity<Page<Book>> getAllBooks() {
    // Usuarios con rol USER o ADMIN
}
```

## 📝 Ejemplo de Respuestas

### ✅ Respuesta Exitosa (200 OK)

```json
{
  "id": 1,
  "title": "El Señor de los Anillos",
  "author": "J.R.R. Tolkien",
  "isbn": "9780544003415",
  "availableCopies": 4,
  "totalCopies": 5
}
```

### ❌ Respuesta sin Token (401 Unauthorized)

```json
{
  "timestamp": "2025-12-01T10:00:00.000+00:00",
  "status": 401,
  "error": "Unauthorized",
  "message": "Full authentication is required to access this resource",
  "path": "/api/books"
}
```

### ❌ Respuesta sin Permisos (403 Forbidden)

```json
{
  "timestamp": "2025-12-01T10:00:00.000+00:00",
  "status": 403,
  "error": "Forbidden",
  "message": "Access Denied",
  "path": "/api/books/1"
}
```

## 🎯 Casos de Uso

### Caso 1: CRUD Completo con Autenticación

**Preparación:**
```
1. Ejecutar: Get Access Token (Manual)
2. Verificar: Token guardado en {{access_token}}
```

**Operaciones:**
```
3. GET /api/books          → Listar libros (requiere token)
4. POST /api/books         → Crear libro (requiere rol ADMIN + token)
5. PUT /api/books/1        → Actualizar libro (requiere rol ADMIN + token)
6. DELETE /api/books/1     → Eliminar libro (requiere rol ADMIN + token)
```

### Caso 2: Búsquedas Autenticadas

**Preparación:**
```
1. Ejecutar: Get Access Token (Manual)
```

**Búsquedas:**
```
2. GET /api/search/title?query=señor  → Búsqueda por título
3. GET /api/available                  → Solo disponibles
4. GET /api/statistics/genre           → Estadísticas por género
```

### Caso 3: Cambio de Usuario en Runtime

**Escenario:** Probar con diferentes roles (USER vs ADMIN)

```
1. Modificar variables:
   - keycloak_username: "user"
   - keycloak_password: "user123"
   
2. Ejecutar: Get Access Token (Manual)
   → Token guardado con rol USER

3. Intentar DELETE /api/books/1
   → Resultado: 403 Forbidden (USER no puede eliminar)

4. Modificar variables:
   - keycloak_username: "admin"
   - keycloak_password: "admin"
   
5. Ejecutar: Get Access Token (Manual)
   → Token guardado con rol ADMIN

6. Intentar DELETE /api/books/1
   → Resultado: 204 No Content (ADMIN puede eliminar)
```

### Caso 4: Sesión de Trabajo Típica

**Inicio del día:**
```
1. Abrir Postman
2. Ejecutar: Get Access Token (Manual)
   → Token válido por 5 minutos
```

**Durante el trabajo (primeros 5 minutos):**
```
3. Probar múltiples endpoints
4. GET All Books → 200 OK
5. POST Create Book → 201 Created
6. PUT Update Book → 200 OK
   → Todos usan el mismo token
```

**Después de 5 minutos:**
```
7. GET All Books → 401 Unauthorized
   (Token expiró)

8. Ejecutar: Get Access Token (Manual)
   → Renovar token

9. GET All Books → 200 OK
   → Continuar trabajando
```

## 📚 Documentación Adicional

- **Keycloak Docs:** https://www.keycloak.org/documentation
- **JWT Docs:** https://jwt.io/introduction
- **Spring Security:** https://spring.io/projects/spring-security
- **Postman Pre-request Scripts:** https://learning.postman.com/docs/writing-scripts/pre-request-scripts/

## 🏆 Ventajas del Método Manual

✅ **Sin bloqueos** - No más "Running Scripts" infinitos  
✅ **Más rápido** - Token solo cuando lo necesitas  
✅ **Mayor control** - Sabes exactamente cuándo se obtiene el token  
✅ **Fácil debugging** - Ves claramente si hay problemas de autenticación  
✅ **Múltiples usuarios** - Cambio fácil de credenciales  
✅ **Production-ready** - Misma configuración para dev/prod  
✅ **Logs descriptivos** - Fácil seguimiento en consola  

## 🔄 Diferencia entre Método Automático vs Manual

### ❌ Método Automático (Deshabilitado)

**Problemas:**
- Se ejecuta antes de CADA petición
- Puede causar bloqueos ("Running Scripts")
- Hace peticiones adicionales innecesarias
- Difícil de depurar cuando falla

### ✅ Método Manual (Actual)

**Ventajas:**
- Solo ejecutas cuando necesitas token nuevo
- Sin bloqueos ni delays
- Control total del proceso
- Fácil identificar errores de autenticación
- Token válido por 5 minutos (múltiples peticiones)  

## 📞 Soporte

### Pasos para Solucionar Problemas

1. **Verificar logs en Postman Console** (Alt+Ctrl+C)
2. **Verificar que Keycloak esté corriendo** (http://localhost:8180)
3. **Verificar variables de colección** (click derecho > Edit > Variables)
4. **Ejecutar manualmente** `Get Access Token` para ver errores específicos
5. **Verificar Gateway** está corriendo (http://localhost:8090/gateway/health)

### Checklist de Verificación

- [ ] ✅ Keycloak corriendo en puerto 8180
- [ ] ✅ Gateway corriendo en puerto 8090
- [ ] ✅ Variables de colección configuradas correctamente
- [ ] ✅ Token obtenido exitosamente (200 OK)
- [ ] ✅ Token guardado en `{{access_token}}`
- [ ] ✅ Autenticación Bearer configurada en la colección

---

**¡Ahora puedes probar todos los endpoints CRUD con autenticación JWT manual y sin bloqueos!** 🎉

**Ventaja principal:** ⚡ **Rápido, confiable y sin "Running Scripts" colgados** ⚡
