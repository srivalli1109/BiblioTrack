package com.library.service;

import com.library.model.BorrowRecord;
import com.library.repository.BorrowRecordRepository;
import com.library.repository.UserRepository;
import com.library.model.User;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class ReminderService {

    @Autowired
    private BorrowRecordRepository borrowRecordRepository;

    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    private UserRepository userRepository;

    // Runs every day at 9:00 AM
    @Scheduled(cron = "0 0 9 * * *")
    public void sendDueDateReminders() {
        LocalDate tomorrow = LocalDate.now().plusDays(1);

        List<BorrowRecord> dueSoon = borrowRecordRepository.findAll().stream()
                .filter(r -> "BORROWED".equals(r.getStatus()))
                .filter(r -> r.getDueDate().equals(tomorrow))
                .toList();

        for (BorrowRecord record : dueSoon) {
            sendReminderEmail(record);
        }
    }

    public void sendReminderEmail(BorrowRecord record) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(record.getUser().getEmail());
        message.setSubject("BiblioTrack: Book Due Tomorrow");
        message.setText(
            "Hi " + record.getUser().getUsername() + ",\n\n" +
            "This is a friendly reminder that \"" + record.getBook().getTitle() + "\" is due tomorrow (" + record.getDueDate() + ").\n\n" +
            "Please return it on time to avoid a late fee.\n\n" +
            "— BiblioTrack Library System"
        );
        mailSender.send(message);
    }
    

// Runs every day at 9:05 AM — checks for books 7+ days overdue and bans the borrower
    @Scheduled(cron = "0 5 9 * * *")
    public void banOverdueUsers() {
    LocalDate today = LocalDate.now();

    List<BorrowRecord> severelyOverdue = borrowRecordRepository.findAll().stream()
            .filter(r -> "BORROWED".equals(r.getStatus()))
            .filter(r -> r.getDueDate().plusDays(7).isBefore(today) || r.getDueDate().plusDays(7).isEqual(today))
            .toList();

    for (BorrowRecord record : severelyOverdue) {
        User user = record.getUser();
        if (!Boolean.TRUE.equals(user.getBanned())) {
            user.setBanned(true);
            userRepository.save(user);
            sendBanNotificationEmail(user, record);
        }
    }
}

private void sendBanNotificationEmail(User user, BorrowRecord record) {
    SimpleMailMessage message = new SimpleMailMessage();
    message.setTo(user.getEmail());
    message.setSubject("BiblioTrack: Account Suspended - Overdue Book");
    message.setText(
        "Hi " + user.getUsername() + ",\n\n" +
        "Your account has been temporarily suspended because \"" + record.getBook().getTitle() + "\" is more than 7 days overdue.\n\n" +
        "Please return the book as soon as possible to restore your borrowing privileges.\n\n" +
        "— BiblioTrack Library System"
    );
    mailSender.send(message);
}
}