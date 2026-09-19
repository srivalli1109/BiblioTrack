package com.library.controller;

import com.library.service.ReminderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/reminders")
public class ReminderController {

    @Autowired
    private ReminderService reminderService;

    @PostMapping("/trigger")
    public String triggerReminders() {
        reminderService.sendDueDateReminders();
        return "Reminder check completed. Emails sent to any members with books due tomorrow.";
    }
    @PostMapping("/trigger-bans")
    public String triggerBans() { 
        reminderService.banOverdueUsers();
        return "Ban check completed. Users with books 7+ days overdue have been suspended.";
}
}