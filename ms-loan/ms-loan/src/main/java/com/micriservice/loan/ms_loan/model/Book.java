package com.micriservice.loan.ms_loan.model;

import jakarta.persistence.*;
import lombok.*;
import java.util.*;

@Entity
@Table(name="books")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Book {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 255)
    private String title;

    @Column(name = "isbn13", length = 13, unique = true)
    private String isbn13;

    // inversa de book_copies.book_id
    @OneToMany(mappedBy = "book")
    private List<BookCopy> copies = new ArrayList<>();

    // *** ESTE CAMPO DEBE EXISTIR PARA QUE 'mappedBy = "authors"' FUNCIONE EN Author ***
    @ManyToMany
    @JoinTable(
            name = "book_authors",
            joinColumns = @JoinColumn(name = "book_id"),
            inverseJoinColumns = @JoinColumn(name = "author_id")
    )
    private Set<Author> authors = new HashSet<>();
}