package com.jomap.backend.Services.Auth;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class TokenBlacklistService {

    private final Map<String, Date> blacklist = new ConcurrentHashMap<>();

    public void blacklistToken(String token, Date expirationDate) {
        if (token != null && expirationDate != null) {
            blacklist.put(token, expirationDate);
        }
    }

    public boolean isBlacklisted(String token) {
        if (token == null) {
            return false;
        }
        Date expirationDate = blacklist.get(token);
        if (expirationDate == null) {
            return false;
        }
        if (expirationDate.before(new Date())) {
            blacklist.remove(token);
            return false;
        }
        return true;
    }

    // Runs every 30 minutes to clean up expired blacklisted tokens
    @Scheduled(fixedRate = 1800000)
    public void cleanupExpiredTokens() {
        Date now = new Date();
        Iterator<Map.Entry<String, Date>> iterator = blacklist.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, Date> entry = iterator.next();
            if (entry.getValue().before(now)) {
                iterator.remove();
            }
        }
    }
}
