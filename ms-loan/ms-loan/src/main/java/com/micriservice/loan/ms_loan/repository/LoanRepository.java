package com.micriservice.loan.ms_loan.repository;

import com.micriservice.loan.ms_loan.model.Loan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface LoanRepository extends JpaRepository<Loan, Long> {

    // Préstamos abiertos (returned_at IS NULL) de un usuario específico
    List<Loan> findByUser_IdAndReturnedAtIsNull(Long userId);

    // Todos los préstamos abiertos
    List<Loan> findByReturnedAtIsNull();

    // Saber si una copia está actualmente prestada
    boolean existsByCopy_IdAndReturnedAtIsNull(Long copyId);

    @Query(
            value = "SELECT * FROM loans WHERE returned_at IS NULL AND due_date < CURDATE()",
            nativeQuery = true
    )
    List<Loan> findOverdueLoansNative();
}