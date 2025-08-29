package com.library.book.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.library.BookDto;
import exceptions.InvalidInputException;
import exceptions.NotFoundException;
import http.ServiceUtil;


@RestController
@RequestMapping("/v1/book")
public class BookController {

    private static final Logger LOGGER = LoggerFactory.getLogger(BookController.class);
    private final ServiceUtil serviceUtil;

    @Autowired
    public BookController(ServiceUtil serviceUtil){
        this.serviceUtil = serviceUtil;
    }

    @GetMapping(value = "/{bookId}", produces = "application/json")
    public BookDto getBook (@PathVariable("bookId") int bookId){
        LOGGER.info("Obteniendo libro por el id: {}", bookId);

        if(bookId < 1){
            LOGGER.info("Se ingreso a menores a 1");
            throw new InvalidInputException("Id de producto invalido: "+ bookId);
        }

        if(bookId == 15){
            LOGGER.info("Se ingreso a iguales a 15");
            throw new NotFoundException("No se encontro producto con id: "+ bookId);
        }
        LOGGER.info("No ingreso a ninguno de los casos");
        return new BookDto(bookId, "El libro misterioso", "978-1-492-00824-9", "Autor ABC", "Fantasia");
    }
}
