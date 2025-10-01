package com.YouTube.Tools.MohdArmanTyagi.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@Service
public class QuotaMonitorService {

    private final AtomicInteger dailyQuotaUsed = new AtomicInteger(0);
    private long lastResetTime = System.currentTimeMillis();
    private static final int MAX_DAILY_QUOTA = 10000; // YouTube API limit

    // Quota costs per operation
    public static final int SEARCH_QUOTA_COST = 112;
    public static final int VIDEO_FETCH_QUOTA_COST = 3;

    public boolean canMakeSearchRequest() {
        resetDailyIfNeeded();
        return getRemainingQuota() >= SEARCH_QUOTA_COST;
    }

    public boolean canMakeVideoFetchRequest() {
        resetDailyIfNeeded();
        return getRemainingQuota() >= VIDEO_FETCH_QUOTA_COST;
    }

    public void trackSearchRequest() {
        resetDailyIfNeeded();
        int used = dailyQuotaUsed.addAndGet(SEARCH_QUOTA_COST);
        log.info("Search request tracked. Quota used: {}/{}", used, MAX_DAILY_QUOTA);
        checkQuotaLevel(used);
    }

    public void trackVideoFetchRequest() {
        resetDailyIfNeeded();
        int used = dailyQuotaUsed.addAndGet(VIDEO_FETCH_QUOTA_COST);
        log.info("Video fetch request tracked. Quota used: {}/{}", used, MAX_DAILY_QUOTA);
        checkQuotaLevel(used);
    }

    public int getRemainingQuota() {
        resetDailyIfNeeded();
        return Math.max(0, MAX_DAILY_QUOTA - dailyQuotaUsed.get());
    }

    public int getQuotaUsed() {
        resetDailyIfNeeded();
        return dailyQuotaUsed.get();
    }

    private void resetDailyIfNeeded() {
        long now = System.currentTimeMillis();
        // Reset every 24 hours
        if (now - lastResetTime > 24 * 60 * 60 * 1000) {
            dailyQuotaUsed.set(0);
            lastResetTime = now;
            log.info("Daily YouTube API quota reset to 0");
        }
    }

    private void checkQuotaLevel(int used) {
        if (used > 9000) {
            log.error("QUOTA CRITICAL: {}/{} - Service will stop soon", used, MAX_DAILY_QUOTA);
        } else if (used > 8000) {
            log.warn("QUOTA WARNING: {}/{} - Approaching limit", used, MAX_DAILY_QUOTA);
        } else if (used > 5000) {
            log.info("QUOTA INFO: {}/{} - Moderate usage", used, MAX_DAILY_QUOTA);
        }
    }

    // Manual reset for testing
    public void resetQuota() {
        dailyQuotaUsed.set(0);
        lastResetTime = System.currentTimeMillis();
    }
}