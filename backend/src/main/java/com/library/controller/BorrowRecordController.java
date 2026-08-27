package com.library.controller;
import com.library.model.Reservation;
import com.library.repository.ReservationRepository;
import com.library.model.Book;
import com.library.model.BorrowRecord;
import com.library.model.User;
import com.library.repository.BookRepository;
import com.library.repository.BorrowRecordRepository;
import com.library.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/borrow")
public class BorrowRecordController {

    @Autowired
    private BorrowRecordRepository borrowRecordRepository;

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ReservationRepository reservationRepository;

    // Get all borrow records
    @GetMapping
    public List<BorrowRecord> getAllRecords() {
        return borrowRecordRepository.findAll();
    }

    // Borrow a book
    @PostMapping("/borrow")
public BorrowRecord borrowBook(@RequestParam Long userId, @RequestParam Long bookId) {
    User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found with id " + userId));
    Book book = bookRepository.findById(bookId)
            .orElseThrow(() -> new RuntimeException("Book not found with id " + bookId));

    if (book.getAvailableQuantity() <= 0) {
        throw new RuntimeException("No copies available for this book");
    }

    book.setAvailableQuantity(book.getAvailableQuantity() - 1);
    bookRepository.save(book);

    BorrowRecord record = new BorrowRecord();
    record.setUser(user);
    record.setBook(book);
    record.setBorrowDate(LocalDate.now());
    record.setDueDate(LocalDate.now().plusDays(14));
    record.setStatus("BORROWED");

    // If this user had a waitlist reservation for this book, mark it fulfilled
    reservationRepository.findByUserIdAndBookIdAndStatus(userId, bookId, "READY")
            .or(() -> reservationRepository.findByUserIdAndBookIdAndStatus(userId, bookId, "WAITING"))
            .ifPresent(reservation -> {
                reservation.setStatus("FULFILLED");
                reservationRepository.save(reservation);
            });

    return borrowRecordRepository.save(record);
}

    // Return a book
    @PutMapping("/return/{recordId}")
public BorrowRecord returnBook(@PathVariable Long recordId) {
    BorrowRecord record = borrowRecordRepository.findById(recordId)
            .orElseThrow(() -> new RuntimeException("Borrow record not found with id " + recordId));

    record.setReturnDate(LocalDate.now());
    record.setStatus("RETURNED");

    Book book = record.getBook();
    book.setAvailableQuantity(book.getAvailableQuantity() + 1);
    bookRepository.save(book);

    // Notify the next person in the waitlist, if any
    var waitlist = reservationRepository.findByBookIdAndStatusOrderByReservedAtAsc(book.getId(), "WAITING");
    if (!waitlist.isEmpty()) {
        Reservation next = waitlist.get(0);
        next.setStatus("READY");
        reservationRepository.save(next);
    }

    return borrowRecordRepository.save(record);
}
}
