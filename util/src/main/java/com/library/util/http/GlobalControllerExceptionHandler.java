package com.library.util.http;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.UNPROCESSABLE_ENTITY;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.support.WebExchangeBindException;

import com.library.util.exceptions.InvalidInputException;
import com.library.util.exceptions.NotFoundException;

import java.util.stream.Collectors;

@RestControllerAdvice
class GlobalControllerExceptionHandler {

	private static final Logger LOGGER = LoggerFactory.getLogger(GlobalControllerExceptionHandler.class);

	@ResponseStatus(NOT_FOUND)
	@ExceptionHandler(NotFoundException.class)
	public @ResponseBody HttpErrorInfo handleNotFoundExceptions(ServerHttpRequest request, NotFoundException ex) {

		return createHttpErrorInfo(NOT_FOUND, request, ex);
	}

	@ResponseStatus(UNPROCESSABLE_ENTITY)
	@ExceptionHandler(InvalidInputException.class)
	public @ResponseBody HttpErrorInfo handleInvalidInputException(ServerHttpRequest request,
			InvalidInputException ex) {

		return createHttpErrorInfo(UNPROCESSABLE_ENTITY, request, ex);
	}

	@ResponseStatus(BAD_REQUEST)
	@ExceptionHandler(WebExchangeBindException.class)
	public @ResponseBody HttpErrorInfo handleValidationExceptions(ServerHttpRequest request,
			WebExchangeBindException ex) {

		// Recopilar todos los errores de validación de los campos
		String errors = ex.getBindingResult()
				.getFieldErrors()
				.stream()
				.map(error -> error.getField() + ": " + error.getDefaultMessage())
				.collect(Collectors.joining(", "));
		
		// Si no hay errores de campo, usar el mensaje general
		if (errors.isEmpty()) {
			errors = ex.getMessage();
		}

		final String path = request.getPath().pathWithinApplication().value();
		LOGGER.debug("Returning HTTP status: {} for path: {}, validation errors: {}", BAD_REQUEST, path, errors);
		
		return new HttpErrorInfo(BAD_REQUEST, path, errors);
	}

	private HttpErrorInfo createHttpErrorInfo(HttpStatus httpStatus, ServerHttpRequest request, Exception ex) {

		final String path = request.getPath().pathWithinApplication().value();
		final String message = ex.getMessage();

		LOGGER.debug("Returning HTTP status: {} for path: {}, message: {}", httpStatus, path, message);
		return new HttpErrorInfo(httpStatus, path, message);
	}
}
