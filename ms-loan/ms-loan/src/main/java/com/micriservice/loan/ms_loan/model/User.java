package com.micriservice.loan.ms_loan.model;

import jakarta.persistence.*;
import lombok.*;

@Entity @Table(name="users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder

public class User {
    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    private Long id;

    @Column(name="full_name", nullable = false,unique = false,length =30)
    private String name;

}
