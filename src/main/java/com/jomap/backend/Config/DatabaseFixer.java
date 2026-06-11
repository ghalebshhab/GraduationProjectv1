package com.jomap.backend.Config;

import jakarta.annotation.PostConstruct;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class DatabaseFixer {

    private final JdbcTemplate jdbcTemplate;

    public DatabaseFixer(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @PostConstruct
    public void fixDatabaseConstraints() {
        try {
            // Drop old constraints that were created when fields were Enums
            jdbcTemplate.execute("ALTER TABLE offers DROP CONSTRAINT IF EXISTS offers_schedule_type_check");
            System.out.println("✅ Successfully dropped constraint 'offers_schedule_type_check'");
            
            jdbcTemplate.execute("ALTER TABLE offers DROP CONSTRAINT IF EXISTS offers_status_check");
            System.out.println("✅ Successfully dropped constraint 'offers_status_check'");

            // Update old Enum string values to match the new Java Enum names
            jdbcTemplate.execute("UPDATE offers SET status = 'ACTIVE' WHERE status = 'APPROVED'");
            jdbcTemplate.execute("UPDATE offers SET status = 'EXPIRED' WHERE status = 'COMPLETED'");
            jdbcTemplate.execute("UPDATE offers SET status = 'ACTIVE' WHERE status = 'PENDING'");
            jdbcTemplate.execute("UPDATE offers SET status = 'CANCELLED' WHERE status = 'REJECTED'");
            System.out.println("✅ Successfully migrated old offer statuses to the new names");

        } catch (Exception e) {
            System.err.println("⚠️ Could not drop constraint or update statuses: " + e.getMessage());
        }
    }
}
