package com.library.controller;

import com.library.model.Book;
import com.library.model.Review;
import com.library.model.User;
import com.library.repository.BookRepository;
import com.library.repository.ReviewRepository;
import com.library.repository.UserRepository;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/reviews")
public class ReviewController {

    @Autowired
    private ReviewRepository reviewRepository;

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private UserRepository userRepository;

    // Get all reviews for a specific book
    @GetMapping("/book/{bookId}")
    public List<Review> getReviewsForBook(@PathVariable Long bookId) {
        return reviewRepository.findByBookId(bookId);
    }

    // Get average rating for a book
    @GetMapping("/book/{bookId}/average")
    public Map<String, Object> getAverageRating(@PathVariable Long bookId) {
        List<Review> reviews = reviewRepository.findByBookId(bookId);
        double average = reviews.stream().mapToInt(Review::getRating).average().orElse(0.0);
        return Map.of(
            "bookId", bookId,
            "averageRating", Math.round(average * 10.0) / 10.0,
            "totalReviews", reviews.size()
        );
    }

    // Submit or update a review (one review per user per book)
    @PostMapping
    public Review submitReview(@RequestParam Long userId, @RequestParam Long bookId, @Valid @RequestBody Review review) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id " + userId));
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new RuntimeException("Book not found with id " + bookId));

        Optional<Review> existing = reviewRepository.findByUserIdAndBookId(userId, bookId);

        Review toSave = existing.orElseGet(Review::new);
        toSave.setUser(user);
        toSave.setBook(book);
        toSave.setRating(review.getRating());
        toSave.setComment(review.getComment());

        return reviewRepository.save(toSave);
    }

    // Delete a review (admin or the review's author could do this; kept simple for now)
    @DeleteMapping("/{id}")
    public String deleteReview(@PathVariable Long id) {
        reviewRepository.deleteById(id);
        return "Review deleted with id: " + id;
    }
}