package com.YouTube.Tools.MohdArmanTyagi.controller;

import com.YouTube.Tools.MohdArmanTyagi.model.Video;
import com.YouTube.Tools.MohdArmanTyagi.service.YouTubeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Controller
@RequestMapping("/api/youtube")
public class YouTubeVideoController {

    @Autowired
    private YouTubeService youTubeService;

    @Value("${youtube.api.key}")
    private String apikey;

    private boolean isApiKeyValid() {
        if (apikey == null || apikey.isEmpty()) {
            return false;
        }

        if (apikey.startsWith("${") && apikey.endsWith("}")) {
            return false;
        }

        if (apikey.contains("youtube.api.key") ||
                apikey.equals("YOUR_API_KEY_HERE") ||
                apikey.equals("YOUR_YOUTUBE_API_KEY_HERE")) {
            return false;
        }

        return true;
    }

    private String extractVideoId(String input) {
        if (input == null || input.trim().isEmpty()) {
            return null;
        }

        String url = input.trim();

        // Handle youtu.be links
        if (url.contains("youtu.be/")) {
            String[] parts = url.split("youtu.be/");
            if (parts.length > 1) {
                String videoId = parts[1].split("[?&]")[0];
                return videoId.length() == 11 ? videoId : null;
            }
        }

        // Handle youtube.com links with v parameter
        if (url.contains("youtube.com") && url.contains("v=")) {
            String[] queryParams = url.split("[?&]");
            for (String param : queryParams) {
                if (param.startsWith("v=")) {
                    String videoId = param.substring(2);
                    return videoId.length() == 11 ? videoId : null;
                }
            }
        }

        // Handle direct video ID (11 characters)
        if (url.matches("[A-Za-z0-9_-]{11}")) {
            return url;
        }

        return null;
    }

    @GetMapping("/video-details")
    public String videoDetailsPage(Model model) {
        log.info("Loading YouTube Video Data Retriever page");

        if (!isApiKeyValid()) {
            String errorMsg = "YouTube API Key is not configured properly. Please check your application.properties file.";
            model.addAttribute("error", errorMsg);
            log.error(errorMsg);
        } else {
            log.info("API key validation successful");
        }

        return "video-details";
    }

    @PostMapping("/video-details")
    @ResponseBody
    public ResponseEntity<?> fetchVideoDetails(@RequestBody Map<String, String> request) {
        String videoUrl = request.get("videoId");
        log.info("Processing video details request for: '{}'", videoUrl);

        // Validate API key
        if (!isApiKeyValid()) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("message", "YouTube API Key is not configured properly.");
            return ResponseEntity.badRequest().body(errorResponse);
        }

        // Validate input
        if (videoUrl == null || videoUrl.trim().isEmpty()) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("message", "Please enter a valid YouTube video URL or ID");
            return ResponseEntity.badRequest().body(errorResponse);
        }

        String input = videoUrl.trim();

        try {
            // Extract video ID from URL
            String videoId = extractVideoId(input);
            if (videoId == null) {
                Map<String, String> errorResponse = new HashMap<>();
                errorResponse.put("message", "Invalid YouTube URL or Video ID. Please check the format.");
                log.warn("Invalid YouTube URL/ID provided: {}", input);
                return ResponseEntity.badRequest().body(errorResponse);
            }

            log.info("Extracted video ID: {}", videoId);

            // Fetch video details
            Video video = youTubeService.getVideoById(videoId);

            if (video == null) {
                Map<String, String> errorResponse = new HashMap<>();
                errorResponse.put("message", "Video not found. Please check the URL or Video ID.");
                log.warn("Video not found for ID: {}", videoId);
                return ResponseEntity.badRequest().body(errorResponse);
            }

            log.info("Video details fetched successfully for: {}", video.getTitle());
            return ResponseEntity.ok(video);

        } catch (Exception e) {
            log.error("Error fetching video details for '{}': {}", input, e.getMessage(), e);
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("message", "Error fetching video details: " + e.getMessage());
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }
}
