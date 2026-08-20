package com.library.controller;
import jakarta.validation.Valid;
import com.library.model.Book;
import com.library.repository.BookRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/books")
public class BookController {

    @Autowired
    private BookRepository bookRepository;

    // Get all books
    @GetMapping
    public List<Book> getAllBooks() {
        return bookRepository.findAll();
    }

    // Get a book by id
    @GetMapping("/{id}")
    public Optional<Book> getBookById(@PathVariable Long id) {
        return bookRepository.findById(id);
    }

    // Add a new book
    @PostMapping
    public Book addBook(@Valid @RequestBody Book book) {
        return bookRepository.save(book);
    }

    // Update a book
    @PutMapping("/{id}")
    public Book updateBook(@PathVariable Long id, @Valid @RequestBody Book updatedBook) {
        return bookRepository.findById(id).map(book -> {
            book.setTitle(updatedBook.getTitle());
            book.setAuthor(updatedBook.getAuthor());
            book.setIsbn(updatedBook.getIsbn());
            book.setGenre(updatedBook.getGenre());
            book.setQuantity(updatedBook.getQuantity());
            book.setAvailableQuantity(updatedBook.getAvailableQuantity());
            return bookRepository.save(book);
    }).orElseThrow(() -> new RuntimeException("Book not found with id " + id));
}

    // Delete a book
    @DeleteMapping("/{id}")
    public String deleteBook(@PathVariable Long id) {
        bookRepository.deleteById(id);
        return "Book deleted with id: " + id;
    }
}