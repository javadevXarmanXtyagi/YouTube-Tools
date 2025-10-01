/*


package com.YouTube.Tools.MohdArmanTyagi.controller;

import com.YouTube.Tools.MohdArmanTyagi.model.SearchVideo;
import com.YouTube.Tools.MohdArmanTyagi.service.YouTubeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/youtube")
public class YouTubeTagsController {
    @Autowired
    private YouTubeService youTubeService;
    @Value("${youtube.api.key}")
    private String apikey;

    private  boolean isApiKeyValid()
    {
        if(apikey == null || apikey.isEmpty())
        {
            return false;
        }
        return true;
    }
    @PostMapping("/search")
    public String videoTags(@RequestParam ("videoTitle") String videoTitle , Model model) {
        if (!isApiKeyValid()) {
            model.addAttribute("error", "Api Key is not Valid");
            return "home";

        }

        if (videoTitle == null || videoTitle.isEmpty()) {
            model.addAttribute("error", "Please enter a valid video title");
            return "home";
        }
        try {
            SearchVideo result = youTubeService.searchVideos(videoTitle);
            model.addAttribute("primaryVideo", result.getPrimaryVideo());
            model.addAttribute("relatedVideos", result.getRelatedVideo());
            return "home";
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            return "home";
        }
    }
}

*//*

package com.YouTube.Tools.MohdArmanTyagi.controller;

import com.YouTube.Tools.MohdArmanTyagi.model.SearchVideo;
import com.YouTube.Tools.MohdArmanTyagi.service.YouTubeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Slf4j
@Controller
@RequestMapping("/youtube")
public class YouTubeTagsController {

    @Autowired
    private YouTubeService youTubeService;

    @Value("${youtube.api.key}")
    private String apikey;

    private boolean isApiKeyValid() {
        if (apikey == null || apikey.isEmpty()) {
            return false;
        }

        // Check for unresolved Spring properties
        if (apikey.startsWith("${") && apikey.endsWith("}")) {
            return false;
        }

        // Check for placeholder values
        if (apikey.contains("youtube.api.key") ||
                apikey.equals("YOUR_API_KEY_HERE") ||
                apikey.equals("YOUR_YOUTUBE_API_KEY_HERE")) {
            return false;
        }

        return true;
    }

    private boolean isYouTubeUrl(String input) {
        if (input == null) return false;

        String lowerInput = input.toLowerCase().trim();
        return lowerInput.contains("youtube.com") ||
                lowerInput.contains("youtu.be") ||
                lowerInput.startsWith("http") ||
                lowerInput.contains("/watch") ||
                lowerInput.contains("?v=") ||
                lowerInput.contains("&v=");
    }

    @GetMapping({"", "/", "/home"})
    public String home(Model model) {
        log.info("Loading YouTube SEO Tag Generator home page");

        if (!isApiKeyValid()) {
            String errorMsg = "YouTube API Key is not configured properly. Please check your application.properties file.";
            model.addAttribute("error", errorMsg);
            log.error(errorMsg + " Current value: {}", apikey);
        } else {
            log.info("API key validation successful");
        }

        return "home";
    }

    @PostMapping("/search")
    public String videoTags(@RequestParam("videoTitle") String videoTitle, Model model) {
        log.info("Processing search request for video title: '{}'", videoTitle);

        // Validate API key
        if (!isApiKeyValid()) {
            String errorMsg = "YouTube API Key is not configured properly. Please check your application.properties file.";
            model.addAttribute("error", errorMsg);
            log.error(errorMsg);
            return "home";
        }

        // Validate input
        if (videoTitle == null || videoTitle.trim().isEmpty()) {
            model.addAttribute("error", "Please enter a valid video title");
            return "home";
        }

        String searchTerm = videoTitle.trim();
        model.addAttribute("searchTerm", searchTerm);

        // Check if input is a URL
        if (isYouTubeUrl(searchTerm)) {
            String errorMsg = "Please enter a video title or keywords to search, not a YouTube URL. " +
                    "For example: 'music tutorial' or 'gaming highlights'";
            model.addAttribute("error", errorMsg);
            log.warn("User entered URL instead of title: {}", searchTerm);
            return "home";
        }

        try {
            log.info("Searching YouTube for: '{}'", searchTerm);
            SearchVideo result = youTubeService.searchVideos(searchTerm);

            // Add results to model
            model.addAttribute("primaryVideo", result.getPrimaryVideo());
            model.addAttribute("relatedVideos", result.getRelatedVideo());

            // Handle results
            if (result.getPrimaryVideo() == null) {
                String noResultsMsg = "No videos found for: '" + searchTerm + "'. Please try a different search term.";
                model.addAttribute("error", noResultsMsg);
                log.warn(noResultsMsg);
            } else {
                int relatedCount = result.getRelatedVideo() != null ? result.getRelatedVideo().size() : 0;
                String successMsg = "Successfully found " + (relatedCount + 1) + " videos (1 primary + " + relatedCount + " related)";
                model.addAttribute("success", successMsg);
                log.info("Search completed successfully. Found {} videos total", relatedCount + 1);
            }

        } catch (Exception e) {
            String errorMsg = "Error searching for videos: " + e.getMessage();
            model.addAttribute("error", errorMsg);
            log.error("Error searching for '{}': {}", searchTerm, e.getMessage(), e);
        }

        return "home";
    }
}*/
package com.YouTube.Tools.MohdArmanTyagi.controller;

import com.YouTube.Tools.MohdArmanTyagi.model.SearchVideo;
import com.YouTube.Tools.MohdArmanTyagi.service.YouTubeService;
import com.YouTube.Tools.MohdArmanTyagi.service.RateLimitService;
import com.YouTube.Tools.MohdArmanTyagi.service.QuotaMonitorService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Map;

@Slf4j
@Controller
@RequestMapping("/youtube")
public class YouTubeTagsController {

    @Autowired
    private YouTubeService youTubeService;

    @Autowired
    private RateLimitService rateLimitService;

    @Autowired
    private QuotaMonitorService quotaMonitorService;

    @Value("${youtube.api.key}")
    private String apikey;

    private boolean isApiKeyValid() {
        if (apikey == null || apikey.isEmpty()) {
            return false;
        }

        // Check for unresolved Spring properties
        if (apikey.startsWith("${") && apikey.endsWith("}")) {
            return false;
        }

        // Check for placeholder values
        if (apikey.contains("youtube.api.key") ||
                apikey.equals("YOUR_API_KEY_HERE") ||
                apikey.equals("YOUR_YOUTUBE_API_KEY_HERE")) {
            return false;
        }

        return true;
    }

    private boolean isYouTubeUrl(String input) {
        if (input == null) return false;

        String lowerInput = input.toLowerCase().trim();
        return lowerInput.contains("youtube.com") ||
                lowerInput.contains("youtu.be") ||
                lowerInput.startsWith("http") ||
                lowerInput.contains("/watch") ||
                lowerInput.contains("?v=") ||
                lowerInput.contains("&v=");
    }

    private String getUserIdentifier(HttpServletRequest request) {
        // Use IP address as user identifier (simplified approach)
        String ipAddress = request.getRemoteAddr();
        if (ipAddress == null || ipAddress.isEmpty() || "0:0:0:0:0:0:0:1".equals(ipAddress)) {
            return "local-user"; // Handle localhost case
        }
        return ipAddress;
    }

    private void addQuotaInfoToModel(Model model, HttpServletRequest request) {
        String userIdentifier = getUserIdentifier(request);
        Map<String, Integer> quotas = rateLimitService.getUserRemainingQuotas(userIdentifier);
        model.addAttribute("remainingSearches", quotas.get("searchesRemaining"));
        model.addAttribute("remainingVideoFetches", quotas.get("videoFetchesRemaining"));
        model.addAttribute("apiQuotaRemaining", quotaMonitorService.getRemainingQuota());
        model.addAttribute("apiQuotaUsed", quotaMonitorService.getQuotaUsed());
    }

    @GetMapping({"", "/", "/home"})
    public String home(Model model, HttpServletRequest request) {
        log.info("Loading YouTube SEO Tag Generator home page");

        if (!isApiKeyValid()) {
            String errorMsg = "YouTube API Key is not configured properly. Please check your application.properties file.";
            model.addAttribute("error", errorMsg);
            log.error(errorMsg + " Current value: {}", apikey);
        } else {
            log.info("API key validation successful");
        }

        // Add quota information to model
        addQuotaInfoToModel(model, request);

        return "home";
    }

    @PostMapping("/search")
    public String videoTags(@RequestParam("videoTitle") String videoTitle, Model model, HttpServletRequest request) {
        log.info("Processing search request for video title: '{}'", videoTitle);

        String userIdentifier = getUserIdentifier(request);

        // Validate API key first
        if (!isApiKeyValid()) {
            String errorMsg = "YouTube API Key is not configured properly. Please check your application.properties file.";
            model.addAttribute("error", errorMsg);
            addQuotaInfoToModel(model, request);
            return "home";
        }

        // Validate input
        if (videoTitle == null || videoTitle.trim().isEmpty()) {
            model.addAttribute("error", "Please enter a valid video title");
            addQuotaInfoToModel(model, request);
            return "home";
        }

        String searchTerm = videoTitle.trim();
        model.addAttribute("searchTerm", searchTerm);

        // Check if input is a URL
        if (isYouTubeUrl(searchTerm)) {
            String errorMsg = "Please enter a video title or keywords to search, not a YouTube URL. " +
                    "For example: 'music tutorial' or 'gaming highlights'";
            model.addAttribute("error", errorMsg);
            addQuotaInfoToModel(model, request);
            return "home";
        }

        // Check user rate limit
        if (!rateLimitService.canMakeSearchRequest(userIdentifier)) {
            model.addAttribute("error", "Daily search limit reached (10 searches per day). Please try again tomorrow.");
            addQuotaInfoToModel(model, request);
            return "home";
        }

        // Check global API quota
        if (!quotaMonitorService.canMakeSearchRequest()) {
            model.addAttribute("error", "YouTube API quota exceeded for today. The service will be available again after midnight UTC.");
            addQuotaInfoToModel(model, request);
            return "home";
        }

        try {
            log.info("Searching YouTube for: '{}' by user {}", searchTerm, userIdentifier);
            SearchVideo result = youTubeService.searchVideos(searchTerm);

            // Track quota usage only if successful
            quotaMonitorService.trackSearchRequest();

            // Add results to model
            model.addAttribute("primaryVideo", result.getPrimaryVideo());
            model.addAttribute("relatedVideos", result.getRelatedVideo());

            // Handle results
            if (result.getPrimaryVideo() == null) {
                String noResultsMsg = "No videos found for: '" + searchTerm + "'. Please try a different search term.";
                model.addAttribute("error", noResultsMsg);
                log.warn(noResultsMsg);
            } else {
                int relatedCount = result.getRelatedVideo() != null ? result.getRelatedVideo().size() : 0;
                String successMsg = "Successfully found " + (relatedCount + 1) + " videos (1 primary + " + relatedCount + " related)";
                model.addAttribute("success", successMsg);
                log.info("Search completed successfully. Found {} videos total for user {}", relatedCount + 1, userIdentifier);
            }

        } catch (Exception e) {
            String errorMsg = "Error searching for videos: " + e.getMessage();
            model.addAttribute("error", errorMsg);
            log.error("Error searching for '{}' by user {}: {}", searchTerm, userIdentifier, e.getMessage(), e);
        }

        // Always add quota info to model
        addQuotaInfoToModel(model, request);
        return "home";
    }
}