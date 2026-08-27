package com.library.controller;

import com.library.model.Book;
import com.library.model.Reservation;
import com.library.model.User;
import com.library.repository.BookRepository;
import com.library.repository.ReservationRepository;
import com.library.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/reservations")
public class ReservationController {

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private UserRepository userRepository;

    // Join the waitlist for a book
    @PostMapping("/join")
    public Reservation joinWaitlist(@RequestParam Long userId, @RequestParam Long bookId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id " + userId));
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new RuntimeException("Book not found with id " + bookId));

        if (book.getAvailableQuantity() > 0) {
            throw new RuntimeException("This book is currently available — no need to join the waitlist.");
        }

        Optional<Reservation> existing = reservationRepository.findByUserIdAndBookIdAndStatus(userId, bookId, "WAITING");
        if (existing.isPresent()) {
            throw new RuntimeException("You are already on the waitlist for this book.");
        }

        Reservation reservation = new Reservation();
        reservation.setUser(user);
        reservation.setBook(book);
        reservation.setStatus("WAITING");

        return reservationRepository.save(reservation);
    }

    // View the waitlist for a specific book (position order)
    @GetMapping("/book/{bookId}")
    public List<Reservation> getWaitlistForBook(@PathVariable Long bookId) {
        return reservationRepository.findByBookIdAndStatusOrderByReservedAtAsc(bookId, "WAITING");
    }

    // View a user's own reservations
    @GetMapping("/user/{userId}")
    public List<Reservation> getUserReservations(@PathVariable Long userId) {
        return reservationRepository.findByUserId(userId);
    }

    // Cancel a reservation
    @PutMapping("/{id}/cancel")
    public Reservation cancelReservation(@PathVariable Long id) {
        Reservation reservation = reservationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Reservation not found with id " + id));
        reservation.setStatus("CANCELLED");
        return reservationRepository.save(reservation);
    }
}