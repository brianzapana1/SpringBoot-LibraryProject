package com.micriservice.loan.ms_loan.model;

import jakarta.persistence.*;

@Entity
@Table(name = "authors")
public class Author {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "full_name")  // 👈 agrega esta anotación
    private String name;         // puedes dejar el nombre del atributo igual
}