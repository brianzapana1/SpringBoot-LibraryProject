package com.micriservice.loan.ms_loan.repository;

import com.micriservice.loan.ms_loan.model.BookCopy;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BookCopyRepository extends JpaRepository<BookCopy, Long> {
}
