package com.micriservice.loan.ms_loan.repository;

import com.micriservice.loan.ms_loan.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
}
