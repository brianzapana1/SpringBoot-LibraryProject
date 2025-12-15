# Evidencias - Config Server y Gateway

## 1. Endpoint `/config/ms-book/default` operativo vía Gateway
- **Comando ejecutado:**
  ```powershell
  curl http://localhost:8090/config/ms-book/default
  ```
- **Resultado esperado / observado:** respuesta JSON (HTTP 200) con la misma carga que entrega directamente el Config Server (`name`, `profiles`, `label`, `propertySources`).
- **Nota:** antes de probar, asegúrate de refrescar Gateway para cargar la última ruta:
  ```powershell
  curl -X POST http://localhost:8090/actuator/refresh
  ```

## 2. Logs del Config Server mostrando `Fetching config from Git`
- **Ubicación del servicio:** módulo `config-server` (puede correrse con `start-config-server.bat`).
- **Cómo ver los logs:**
  ```powershell
  # En una terminal donde se lanzó el config-server
  Get-Content .\config-server\target\config-server.log -Wait
  ```
- **Entrada clave:**
  ```text
  ... ConfigServer  : Fetching config from server at: https://github.com/Fesur/config-repo
  ... ConfigServer  : Adding property source: https://github.com/Fesur/config-repo/ms-book.yml
  ```
  Esto confirma que el servidor está leyendo la configuración desde el nuevo repositorio en GitHub.

## 3. Archivos YAML general y específico funcionando
- **General (`application.yml`):** `config-repo/application.yml` define propiedades comunes (por ejemplo, puertos, CORS y logging).
- **Específico (`ms-book.yml`):** `config-repo/ms-book.yml` contiene datos exclusivos del microservicio: datasource PostgreSQL, JWT, Eureka, SpringDoc, etc. Se validó que este archivo se entrega correctamente tanto directo (`http://localhost:8888/ms-book/default`) como vía Gateway (`/config/ms-book/default`).

## 4. Gateway, Config Server, Eureka y microservicios levantados en conjunto
1. Iniciar todos los servicios (por ejemplo con `start-all-services.bat` o `start-complete-system.ps1`).
2. Verificar salud rápidamente:
   ```powershell
   curl http://localhost:8761        # Eureka UI
   curl http://localhost:8888/actuator/health -u config:config123   # Config Server
   curl http://localhost:8090/gateway/health                       # Gateway
   curl http://localhost:8090/api/books                            # MS-Book vía Gateway (token requerido)
   ```
3. Opcional: ejecutar `system-status.ps1` para listar el estado de cada servicio.

## 5. Propiedad cifrada visible y descifrada
- **Propiedad cifrada:** `app.gateway.api-key` (y `app.jwt.secret`) en `config-repo/gateway.yml` se almacenan con `{cipher}`.
- **Comprobación vía Config Server:**
  ```powershell
  curl http://localhost:8090/config/gateway/default
  ```
  La respuesta muestra la propiedad ya **descifrada** (texto plano) gracias a la clave definida en `config-server/src/main/resources/application.yml` (`encrypt.key`).
- **Uso en el microservicio:** el Gateway consume esta propiedad en tiempo de ejecución para sus integraciones, demostrando que el descifrado funcionó y el servicio continúa respondiendo correctamente.

## 6. Resumen final
- ✅ Endpoint de configuración accesible directo y vía Gateway (con autorización básica propagada).
- ✅ Logs evidencian lectura desde Git (`https://github.com/Fesur/config-repo`).
- ✅ Archivos `application.yml` y `ms-book.yml` entregan la configuración adecuada.
- ✅ Todos los servicios (Gateway, Config Server, Eureka, MS-Book) pueden levantarse juntos sin errores.
- ✅ Propiedad cifrada almacenada, visible descifrada en el endpoint y aplicada por el microservicio.

Estas evidencias cubren los puntos solicitados y dejan listo el entorno para su validación o demo.
