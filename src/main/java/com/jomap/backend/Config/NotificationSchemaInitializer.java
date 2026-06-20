package com.jomap.backend.Config;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class NotificationSchemaInitializer implements ApplicationRunner {

    private final JdbcTemplate jdbcTemplate;

    public NotificationSchemaInitializer(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void run(ApplicationArguments args) {
        addRejectionReasonColumnIfMissing();
    }

    private void addRejectionReasonColumnIfMissing() {
        try {
            jdbcTemplate.execute("ALTER TABLE notifications ADD COLUMN rejection_reason VARCHAR(2000)");
            System.out.println("Added notifications.rejection_reason column successfully.");
        } catch (Exception e) {
            String message = e.getMessage() == null ? "" : e.getMessage().toLowerCase();

            if (message.contains("duplicate")
                    || message.contains("already exists")
                    || message.contains("exists")
                    || message.contains("column") && message.contains("rejection_reason")) {
                System.out.println("notifications.rejection_reason column already exists.");
                return;
            }

            throw e;
        }
    }
}
