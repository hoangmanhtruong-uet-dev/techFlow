package com.techflow.reminder.scheduler;

import com.techflow.reminder.model.Reminder;
import com.techflow.reminder.model.ReminderStatus;
import com.techflow.reminder.repository.ReminderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class ReminderScheduler {

    private final ReminderRepository reminderRepository;

    /**
     * BR-REMINDER-001: Scheduler định kỳ quét các reminder PENDING có remind_at <= now()
     * Quét mỗi phút (fixedDelay = 60000ms = 1 phút)
     */
    @Scheduled(fixedDelayString = "${techflow.scheduler.reminder-interval:60000}")
    @Transactional
    public void scanAndTriggerReminders() {
        Instant now = Instant.now();
        List<Reminder> dueReminders = reminderRepository.findByStatusAndRemindAtLessThanEqual(ReminderStatus.PENDING, now);

        if (!dueReminders.isEmpty()) {
            log.info("Found {} due reminders to process at {}", dueReminders.size(), now);
            for (Reminder reminder : dueReminders) {
                try {
                    log.info("[NOTIFICATION] Reminder triggered for task '{}' (id: {}), user '{}', remindAt: {}",
                            reminder.getTask().getTitle(),
                            reminder.getTask().getId(),
                            reminder.getTask().getUser().getEmail(),
                            reminder.getRemindAt());

                    reminder.setStatus(ReminderStatus.SENT);
                    reminderRepository.save(reminder);
                } catch (Exception e) {
                    log.error("Failed to process reminder {}: {}", reminder.getId(), e.getMessage(), e);
                    reminder.setStatus(ReminderStatus.FAILED);
                    reminderRepository.save(reminder);
                }
            }
        }
    }
}