package com.YouTube.Tools.MohdArmanTyagi.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
public class RateLimitService {

    private final Map<String, UserQuota> userQuotas = new ConcurrentHashMap<>();
    private long lastResetTime = System.currentTimeMillis();

    // Limits per user per day
    private static final int MAX_SEARCHES_PER_DAY = 10;
    private static final int MAX_VIDEO_FETCHES_PER_DAY = 20;

    public boolean canMakeSearchRequest(String userIdentifier) {
        resetDailyIfNeeded();
        UserQuota quota = userQuotas.computeIfAbsent(userIdentifier, k -> new UserQuota());

        if (quota.getSearchesToday() >= MAX_SEARCHES_PER_DAY) {
            log.warn("User {} exceeded search limit: {}/{}",
                    userIdentifier, quota.getSearchesToday(), MAX_SEARCHES_PER_DAY);
            return false;
        }

        quota.incrementSearches();
        log.debug("User {} search count: {}/{}",
                userIdentifier, quota.getSearchesToday(), MAX_SEARCHES_PER_DAY);
        return true;
    }

    public boolean canMakeVideoFetchRequest(String userIdentifier) {
        resetDailyIfNeeded();
        UserQuota quota = userQuotas.computeIfAbsent(userIdentifier, k -> new UserQuota());

        if (quota.getVideoFetchesToday() >= MAX_VIDEO_FETCHES_PER_DAY) {
            log.warn("User {} exceeded video fetch limit: {}/{}",
                    userIdentifier, quota.getVideoFetchesToday(), MAX_VIDEO_FETCHES_PER_DAY);
            return false;
        }

        quota.incrementVideoFetches();
        log.debug("User {} video fetch count: {}/{}",
                userIdentifier, quota.getVideoFetchesToday(), MAX_VIDEO_FETCHES_PER_DAY);
        return true;
    }

    public Map<String, Integer> getUserRemainingQuotas(String userIdentifier) {
        resetDailyIfNeeded();
        UserQuota quota = userQuotas.get(userIdentifier);

        if (quota == null) {
            return Map.of(
                    "searchesRemaining", MAX_SEARCHES_PER_DAY,
                    "videoFetchesRemaining", MAX_VIDEO_FETCHES_PER_DAY
            );
        }

        return Map.of(
                "searchesRemaining", Math.max(0, MAX_SEARCHES_PER_DAY - quota.getSearchesToday()),
                "videoFetchesRemaining", Math.max(0, MAX_VIDEO_FETCHES_PER_DAY - quota.getVideoFetchesToday())
        );
    }

    private void resetDailyIfNeeded() {
        long now = System.currentTimeMillis();
        // Reset every 24 hours
        if (now - lastResetTime > 24 * 60 * 60 * 1000) {
            userQuotas.clear();
            lastResetTime = now;
            log.info("Daily quotas reset");
        }
    }

    private static class UserQuota {
        private int searchesToday = 0;
        private int videoFetchesToday = 0;

        public int getSearchesToday() { return searchesToday; }
        public int getVideoFetchesToday() { return videoFetchesToday; }

        public void incrementSearches() { searchesToday++; }
        public void incrementVideoFetches() { videoFetchesToday++; }
    }
}
