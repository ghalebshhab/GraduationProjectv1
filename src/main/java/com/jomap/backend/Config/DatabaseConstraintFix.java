package com.jomap.backend.Config;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component
public class DatabaseConstraintFix {

    private static final Logger logger = LoggerFactory.getLogger(DatabaseConstraintFix.class);

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @PostConstruct
    public void fixConstraints() {
        try {
            logger.info("Checking and dropping old posts_type_check constraint if it exists...");
            jdbcTemplate.execute("ALTER TABLE posts DROP CONSTRAINT IF EXISTS posts_type_check;");
            logger.info("Successfully dropped posts_type_check constraint.");
        } catch (Exception e) {
            logger.warn("Could not drop posts_type_check constraint. It may not exist or another error occurred: {}", e.getMessage());
        }
    }
}
