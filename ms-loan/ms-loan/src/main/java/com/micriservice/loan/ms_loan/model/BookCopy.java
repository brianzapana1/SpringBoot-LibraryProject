package com.micriservice.loan.ms_loan.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "book_copies")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookCopy {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Relación con la tabla books
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "book_id", nullable = false)
    private Book book;

    // Código único de inventario (ej: BC-001)
    @Column(name = "code", length = 40, unique = true)
    private String code;

    // ENUM que mapea a la columna status ('AVAILABLE', 'LOANED')
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 10)
    private Status status = Status.AVAILABLE;

    // Definición del ENUM (coincide con los valores en MySQL)
    public enum Status {
        AVAILABLE,
        LOANED
    }
}