package com.jomap.backend.Config;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;

@Component
public class NotificationSchemaInitializer implements ApplicationRunner {

    private final JdbcTemplate jdbcTemplate;
    private final DataSource dataSource;

    public NotificationSchemaInitializer(JdbcTemplate jdbcTemplate, DataSource dataSource) {
        this.jdbcTemplate = jdbcTemplate;
        this.dataSource = dataSource;
    }

    @Override
    public void run(ApplicationArguments args) {
        ensureRejectionReasonColumnExists();
        backfillExistingRejectionReasons();
        createMissingLocationRejectionNotifications();
    }

    private void ensureRejectionReasonColumnExists() {
        if (columnExists("notifications", "rejection_reason")) {
            System.out.println("notifications.rejection_reason column already exists.");
            return;
        }

        jdbcTemplate.execute("ALTER TABLE notifications ADD COLUMN rejection_reason VARCHAR(2000)");
        System.out.println("Added notifications.rejection_reason column successfully.");
    }

    private boolean columnExists(String tableName, String columnName) {
        try (Connection connection = dataSource.getConnection()) {
            DatabaseMetaData metaData = connection.getMetaData();

            String[] tableCandidates = {
                    tableName,
                    tableName.toLowerCase(),
                    tableName.toUpperCase()
            };

            String[] columnCandidates = {
                    columnName,
                    columnName.toLowerCase(),
                    columnName.toUpperCase()
            };

            for (String table : tableCandidates) {
                for (String column : columnCandidates) {
                    try (ResultSet resultSet = metaData.getColumns(connection.getCatalog(), null, table, column)) {
                        if (resultSet.next()) {
                            return true;
                        }
                    }
                }
            }
        } catch (Exception e) {
            throw new IllegalStateException("Could not check notifications.rejection_reason column", e);
        }

        return false;
    }

    private void backfillExistingRejectionReasons() {
        try {
            int updatedRows = jdbcTemplate.update("""
                    UPDATE notifications n
                    JOIN location_lists l ON n.location_id = l.id
                    SET n.rejection_reason = l.rejection_reason,
                        n.text = CONCAT('تم رفض طلب اعتماد منشأة \"', COALESCE(NULLIF(TRIM(l.name), ''), 'منشأتك'), '\". السبب: ', l.rejection_reason)
                    WHERE l.rejection_reason IS NOT NULL
                      AND TRIM(l.rejection_reason) <> ''
                      AND (n.rejection_reason IS NULL OR TRIM(n.rejection_reason) = '')
                    """);
            System.out.println("Backfilled notifications.rejection_reason rows: " + updatedRows);
        } catch (Exception mysqlSyntaxError) {
            int updatedRows = jdbcTemplate.update("""
                    UPDATE notifications n
                    SET rejection_reason = l.rejection_reason,
                        text = CONCAT('تم رفض طلب اعتماد منشأة \"', COALESCE(NULLIF(TRIM(l.name), ''), 'منشأتك'), '\". السبب: ', l.rejection_reason)
                    FROM location_lists l
                    WHERE n.location_id = l.id
                      AND l.rejection_reason IS NOT NULL
                      AND TRIM(l.rejection_reason) <> ''
                      AND (n.rejection_reason IS NULL OR TRIM(n.rejection_reason) = '')
                    """);
            System.out.println("Backfilled notifications.rejection_reason rows: " + updatedRows);
        }
    }

    private void createMissingLocationRejectionNotifications() {
        try {
            int insertedRows = jdbcTemplate.update("""
                    INSERT INTO notifications (text, type, category, to_user_id, location_id, rejection_reason, is_read, created_at)
                    SELECT
                        CONCAT('تم رفض طلب اعتماد منشأة \"', COALESCE(NULLIF(TRIM(l.name), ''), 'منشأتك'), '\". السبب: ', l.rejection_reason),
                        'SYSTEM',
                        'OWNER',
                        l.owner_id,
                        l.id,
                        l.rejection_reason,
                        false,
                        CURRENT_TIMESTAMP
                    FROM location_lists l
                    WHERE l.status = 'REJECTED'
                      AND l.owner_id IS NOT NULL
                      AND l.rejection_reason IS NOT NULL
                      AND TRIM(l.rejection_reason) <> ''
                      AND NOT EXISTS (
                          SELECT 1
                          FROM notifications n
                          WHERE n.location_id = l.id
                            AND n.type = 'SYSTEM'
                            AND n.rejection_reason = l.rejection_reason
                      )
                    """);
            System.out.println("Created missing location rejection notifications: " + insertedRows);
        } catch (Exception e) {
            throw new IllegalStateException("Could not create missing location rejection notifications", e);
        }
    }
}
