# ⚡ Guía Rápida - Postman JWT

## 🚀 Inicio Rápido (3 pasos)

### 1️⃣ Importar Colección
```
1. Abrir Postman
2. Import → Seleccionar: Library-Management-API.postman_collection.json
3. ✅ Colección importada
```

### 2️⃣ Obtener Token JWT
```
1. Ir a carpeta: Keycloak - Authentication
2. Ejecutar: Get Access Token (Manual)
3. Verificar: 200 OK
4. ✅ Token guardado automáticamente
```

### 3️⃣ Probar Endpoints
```
1. Ir a: Books - CRUD Operations
2. Ejecutar cualquier petición (ej: GET All Books)
3. ✅ Funciona con autenticación automática
```

---

## 🔑 Cómo Usar

### Obtener Token (Primera vez o cuando expire)

```
Keycloak - Authentication > Get Access Token (Manual) > Send
```

**Resultado esperado:**
```json
{
  "access_token": "eyJhbGciOiJSUzI1NiIsInR5cC...",
  "expires_in": 300,
  "token_type": "Bearer"
}
```

✅ El token se guarda automáticamente en `{{access_token}}`

### Usar Endpoints (Con token ya obtenido)

```
Books - CRUD Operations > GET All Books > Send
```

**El token se incluye automáticamente en:**
```
Authorization: Bearer {{access_token}}
```

### Renovar Token (Cada 5 minutos)

Si recibes `401 Unauthorized`:

```
1. Ir a: Get Access Token (Manual)
2. Click Send
3. ✅ Token renovado
4. Reintentar el endpoint que falló
```

---

## 📋 Endpoints Principales

### 1. Autenticación (USAR PRIMERO) 🔐

| Endpoint | Descripción |
|----------|-------------|
| **Get Access Token (Manual)** | ⭐ Obtener token JWT |
| Refresh Token | Renovar con refresh_token |
| Test Endpoint Protected | Verificar protección (debe dar 401) |

### 2. CRUD de Libros 📚

| Endpoint | Método | Requiere Token | Requiere Rol |
|----------|--------|----------------|--------------|
| GET All Books | GET | ✅ Sí | USER |
| GET Book by ID | GET | ✅ Sí | USER |
| POST Create Book | POST | ✅ Sí | ADMIN |
| PUT Update Book | PUT | ✅ Sí | ADMIN |
| DELETE Book | DELETE | ✅ Sí | ADMIN |

### 3. Búsquedas 🔍

| Endpoint | Descripción |
|----------|-------------|
| Search by Title | Buscar por título |
| Search by Author | Buscar por autor |
| Full Text Search | Búsqueda en todos los campos |
| Get Available Books | Solo libros disponibles |

---

## ⚠️ Solución Rápida de Problemas

### ❌ Postman se queda "Running Scripts"

**✅ SOLUCIONADO:** El script automático fue deshabilitado. Ahora usas método manual.

### ❌ Error 401 Unauthorized

**Solución:**
```
Get Access Token (Manual) > Send > Reintentar
```

### ❌ Error 403 Forbidden

**Causa:** Usuario no tiene permisos (ej: USER intentando DELETE)

**Solución:** Cambiar a usuario con rol ADMIN
```
1. Click derecho en colección > Edit > Variables
2. Cambiar keycloak_username a "admin"
3. Cambiar keycloak_password a "admin"
4. Save
5. Get Access Token (Manual) > Send
6. Reintentar operación
```

### ❌ No se pudo obtener token

**Verificar:**
```powershell
# Keycloak debe estar corriendo:
curl http://localhost:8180
```

---

## 📊 Variables Importantes

| Variable | Valor por Defecto |
|----------|-------------------|
| `gateway_url` | http://localhost:8090 |
| `keycloak_url` | http://localhost:8180 |
| `keycloak_realm` | library-realm |
| `keycloak_client_id` | library-client |
| `keycloak_username` | admin |
| `keycloak_password` | admin |
| `access_token` | (auto) |

**Para modificar:**
```
Click derecho en colección > Edit > Variables > Modificar > Save
```

---

## 🎯 Flujo de Trabajo Típico

```
DÍA 1:
------
1. Abrir Postman
2. Get Access Token (Manual) → Token válido por 5 min
3. GET All Books → 200 OK
4. POST Create Book → 201 Created
5. PUT Update Book → 200 OK
6. DELETE Book → 204 No Content

(Después de 5 minutos)

7. GET All Books → 401 Unauthorized
8. Get Access Token (Manual) → Renovar token
9. GET All Books → 200 OK
10. Continuar trabajando...
```

---

## 💡 Tips

### ✅ Buenas Prácticas

1. **Obtener token al inicio del día**
2. **Renovar cada 5 minutos** (cuando recibas 401)
3. **Verificar Keycloak está corriendo** antes de empezar
4. **Usar Postman Console** para ver logs (Alt+Ctrl+C)
5. **Guardar cambios** en variables después de modificar usuarios

### ⚡ Atajos de Postman

| Atajo | Acción |
|-------|--------|
| `Ctrl + Enter` | Enviar petición |
| `Alt + Ctrl + C` | Abrir consola |
| `Ctrl + E` | Abrir entorno/variables |

---

## 📞 Ayuda Rápida

### Servicios Necesarios

```powershell
# Verificar Keycloak:
curl http://localhost:8180

# Verificar Gateway:
curl http://localhost:8090/gateway/health
```

### Ver Token Decodificado

1. Ejecutar: `Get Access Token (Manual)`
2. Copiar valor de `access_token`
3. Ir a: https://jwt.io
4. Pegar token
5. Ver información (usuario, roles, expiración)

---

**¿Necesitas más ayuda?** Lee `POSTMAN_JWT_README.md` para documentación completa.

---

✅ **Todo listo para usar la API con autenticación JWT** ✅
