package com.library.service;

import com.library.model.BorrowRecord;
import com.library.repository.BorrowRecordRepository;
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
}