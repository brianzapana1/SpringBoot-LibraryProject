package com.micriservice.loan.ms_loan.service;

import com.micriservice.loan.ms_loan.dto.LoanResponse;
import com.micriservice.loan.ms_loan.model.BookCopy;
import com.micriservice.loan.ms_loan.model.Loan;
import com.micriservice.loan.ms_loan.model.User;
import com.micriservice.loan.ms_loan.repository.BookCopyRepository;
import com.micriservice.loan.ms_loan.repository.LoanRepository;
import com.micriservice.loan.ms_loan.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
@RequiredArgsConstructor
public class LoanService {

    private final LoanRepository loanRepo;
    private final BookCopyRepository copyRepo;
    private final UserRepository userRepo;

    /** Crea un préstamo abierto (returnedAt == null). */
    @Transactional
    public Loan create(Long userId, Long copyId, LocalDate loanDate, LocalDate dueDate) {
        if (loanDate == null || dueDate == null) {
            throw new IllegalArgumentException("loanDate y dueDate son obligatorios");
        }
        if (!dueDate.isAfter(loanDate)) {
            throw new IllegalArgumentException("dueDate debe ser posterior a loanDate");
        }

        User user = userRepo.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado: " + userId));

        BookCopy copy = copyRepo.findById(copyId)
                .orElseThrow(() -> new IllegalArgumentException("Copia no encontrada: " + copyId));

        // Si manejas estado de copias, valida disponibilidad aquí
        if (copy.getStatus() != null && copy.getStatus() != BookCopy.Status.AVAILABLE) {
            throw new IllegalStateException("La copia no está disponible");
        }

        // Marcar como prestada si usas status
        if (copy.getStatus() != null) {
            copy.setStatus(BookCopy.Status.LOANED);
            copyRepo.save(copy);
        }

        Loan loan = Loan.builder()
                .user(user)
                .copy(copy)
                .loanDate(loanDate)
                .dueDate(dueDate)
                .returnedAt(null)
                .build();

        return loanRepo.save(loan);
    }

    /** Obtiene un préstamo por id. */
    @Transactional(readOnly = true)
    public Loan get(Long id) {
        return loanRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Préstamo no encontrado: " + id));
    }

    /** Devuelve/cierra el préstamo (idempotente). */
    @Transactional
    public Loan returnLoan(Long loanId, LocalDate returnedAt) {
        Loan loan = get(loanId);

        if (loan.getReturnedAt() != null) {
            return loan; // ya devuelto
        }

        loan.setReturnedAt(returnedAt != null ? returnedAt : LocalDate.now());

        // Liberar la copia si manejas status
        BookCopy copy = loan.getCopy();
        if (copy.getStatus() != null) {
            copy.setStatus(BookCopy.Status.AVAILABLE);
            copyRepo.save(copy);
        }

        return loanRepo.save(loan);
    }

    /** Préstamos abiertos de un usuario. */
    @Transactional(readOnly = true)
    public List<Loan> listOpenByUser(Long userId) {
        return loanRepo.findByUser_IdAndReturnedAtIsNull(userId);   // ✅ user.id
    }

    /** Préstamos vencidos (abiertos y dueDate < hoy). */
    @Transactional(readOnly = true)
    public List<Loan> listOverdue(LocalDate today) {
        return loanRepo.findByReturnedAtIsNull().stream()
                .filter(l -> today.isAfter(l.getDueDate()))
                .toList();
    }

    //Derived Query //
    @Transactional(readOnly = true)
    public List<Loan> getLoansByUser(Long userId) {
        return loanRepo.findByUser_IdAndReturnedAtIsNull(userId);
    }

    /** Native Query*/
    @Transactional(readOnly = true)
    public List<Loan> getOverdueLoans() {
        return loanRepo.findOverdueLoansNative();
    }




    /** Días de diferencia (fecha-calendario). */
    public static int diffInDays(LocalDate start, LocalDate due) {
        return (int) ChronoUnit.DAYS.between(start, due);
    }
}