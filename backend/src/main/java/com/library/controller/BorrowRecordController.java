package com.library.controller;

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
        record.setDueDate(LocalDate.now().plusDays(14)); // 2-week borrow period
        record.setStatus("BORROWED");

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

        return borrowRecordRepository.save(record);
    }
}