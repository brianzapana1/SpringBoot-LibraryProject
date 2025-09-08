package bo.edu.ucb.microservices.core.auth.ms_auth.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import bo.edu.ucb.microservices.dto.auth.AuthDto;
import bo.edu.ucb.microservices.util.exceptions.InvalidInputException;
import bo.edu.ucb.microservices.util.exceptions.NotFoundException;
import bo.edu.ucb.microservices.util.http.HttpErrorInfo;
import bo.edu.ucb.microservices.util.http.ServiceUtil;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/v1/auth")
@Tag(name = "Auth", description = "REST API para autenticación de usuarios")
public class AuthServiceController {

    private static final Logger LOGGER = LoggerFactory.getLogger(AuthServiceController.class);
    private final ServiceUtil serviceUtil;

    @Autowired
    public AuthServiceController(ServiceUtil serviceUtil) {
        this.serviceUtil = serviceUtil;
    }

    // ======================= POST =======================
    @Operation(
            summary = "Inicia sesión de usuario",
            description = "Recibe credenciales (username y password) y retorna los datos básicos del usuario autenticado."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Inicio de sesión exitoso",
                    content = @Content(schema = @Schema(implementation = AuthDto.class))),
            @ApiResponse(responseCode = "400", description = "Credenciales inválidas",
                    content = @Content(schema = @Schema(implementation = HttpErrorInfo.class)))
    })
    @PostMapping(value = "/login", consumes = "application/json", produces = "application/json")
    public AuthDto login(
            @Parameter(description = "Credenciales del usuario", required = true)
            @RequestBody AuthDto authDto
    ) {
        LOGGER.info("Intentando iniciar sesión con username: {}", authDto.getUsername());

        if (authDto.getUsername() == null || authDto.getUsername().isBlank()) {
            throw new InvalidInputException("El nombre de usuario no puede estar vacío");
        }

        if (authDto.getPassword() == null || authDto.getPassword().isBlank()) {
            throw new InvalidInputException("La contraseña no puede estar vacía");
        }

        return new AuthDto(authDto.getUsername(), authDto.getPassword());
    }

    // ======================= GET =======================
    @Operation(
            summary = "Obtiene un usuario por su username",
            description = "Retorna la información básica de un usuario autenticado."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Usuario encontrado",
                    content = @Content(schema = @Schema(implementation = AuthDto.class))),
            @ApiResponse(responseCode = "404", description = "Usuario no encontrado",
                    content = @Content(schema = @Schema(implementation = HttpErrorInfo.class)))
    })
    @GetMapping(value = "/{username}", produces = "application/json")
    public AuthDto getUser(
            @Parameter(description = "Username del usuario", required = true)
            @PathVariable("username") String username
    ) {
        LOGGER.info("Buscando usuario con username: {}", username);

        if (username.equalsIgnoreCase("notfound")) {
            throw new NotFoundException("Usuario no encontrado con username: " + username);
        }

        return new AuthDto(username, "***");
    }

    // ======================= PUT =======================
    @Operation(
            summary = "Actualiza un usuario existente",
            description = "Permite actualizar los datos de un usuario."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Usuario actualizado correctamente",
                    content = @Content(schema = @Schema(implementation = AuthDto.class))),
            @ApiResponse(responseCode = "400", description = "Datos inválidos",
                    content = @Content(schema = @Schema(implementation = HttpErrorInfo.class)))
    })
    @PutMapping(value = "/{username}", consumes = "application/json", produces = "application/json")
    public AuthDto updateUser(
            @Parameter(description = "Username del usuario a actualizar", required = true)
            @PathVariable("username") String username,
            @Parameter(description = "Nuevos datos del usuario", required = true)
            @RequestBody AuthDto authDto
    ) {
        LOGGER.info("Actualizando usuario con username: {}", username);

        if (authDto.getPassword() == null || authDto.getPassword().isBlank()) {
            throw new InvalidInputException("La contraseña no puede estar vacía");
        }

        return new AuthDto(username, "***");
    }

    // ======================= DELETE =======================
    @Operation(
            summary = "Elimina un usuario",
            description = "Permite eliminar un usuario del sistema según su username."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Usuario eliminado correctamente"),
            @ApiResponse(responseCode = "404", description = "Usuario no encontrado",
                    content = @Content(schema = @Schema(implementation = HttpErrorInfo.class)))
    })
    @DeleteMapping(value = "/{username}")
    public String deleteUser(
            @Parameter(description = "Username del usuario a eliminar", required = true)
            @PathVariable("username") String username
    ) {
        LOGGER.info("Eliminando usuario con username: {}", username);

        if (username.equalsIgnoreCase("notfound")) {
            throw new NotFoundException("Usuario no encontrado con username: " + username);
        }

        return "Usuario con username '" + username + "' ha sido eliminado correctamente.";
    }
}
