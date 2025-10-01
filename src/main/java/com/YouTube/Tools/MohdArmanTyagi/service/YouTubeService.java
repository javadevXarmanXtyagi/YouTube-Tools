/*
package com.YouTube.Tools.MohdArmanTyagi.service;

import com.YouTube.Tools.MohdArmanTyagi.model.SearchVideo;
import com.YouTube.Tools.MohdArmanTyagi.model.Video;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class YouTubeService {
    private final WebClient.Builder webClientBuilder;

    @Value("${youtube.api.key}")
    private String apikey;

    @Value("${youtube.api.base.url}")
    private String baseUrl;

    @Value("${youtube.api.max.related.videos}")
    private int maxRelatedVideos;

    public SearchVideo searchVideos(String videoTitle) {
        List<String> videoIds = searchForVideoIds(videoTitle);
        if(videoIds.isEmpty()){
            return SearchVideo.builder()
                    .primaryVideo(null)
                    .relatedVideo(Collections.emptyList())
                    .build();
        }
        String primaryVideoId=videoIds.get(0);
        List<String> relatedVideoIds = videoIds.subList(1, Math.min(videoIds.size(), maxRelatedVideos + 1));
        Video primaryVideo=getVideoById(primaryVideoId);
        List<Video> relatedVideos = new ArrayList<>();
        for (String id: relatedVideoIds){
            Video video = getVideoById(id);

            if(video!=null){
                relatedVideos.add(video);
            }
        }
        return SearchVideo.builder()
                .primaryVideo(primaryVideo)
                .relatedVideo(relatedVideos)
                .build();
    }

    private List<String> searchForVideoIds(String videoTitle) {
        SearchApiResponse response = webClientBuilder
                .baseUrl(baseUrl)  // ADD THIS LINE
                .build()
                .get()
                .uri(uriBuilder -> uriBuilder
                        .path("/search")
                        .queryParam("part", "snippet")
                        .queryParam("q", videoTitle)
                        .queryParam("type", "video")
                        .queryParam("maxResults", maxRelatedVideos + 1)  // +1 for primary video
                        .queryParam("key", apikey)
                        .build())
                .retrieve()
                .bodyToMono(SearchApiResponse.class)
                .block();

        if(response==null|| response.items==null)
        {
            return Collections.emptyList();
        }

        List<String> videoIds=new ArrayList<>();
        for (SearchItem item : response.items)
        {
            if (item.id != null && item.id.videoId != null) {  // Add null check
                videoIds.add(item.id.videoId);
            }
        }

        return videoIds;
    }
    private Video getVideoById(String videoId){
        VideoApiResponse response = webClientBuilder.baseUrl(baseUrl).build()
                .get()
                .uri(uriBuilder -> uriBuilder
                        .path("/videos")
                        .queryParam("part","snippet")
                        .queryParam("id",videoId)
                        .queryParam("key",apikey)
                        .build())
                .retrieve()
                .bodyToMono(VideoApiResponse.class)
                .block();

        if (response==null|| response.items==null)
        {
            return null;
        }
        Snippet snippet = response.items.get(0).snippet;
        return Video.builder()
                .id(videoId)
                .channelTitle(snippet.channelTitle)
                .title(snippet.title)
                .tags(snippet.tags==null? Collections.emptyList(): snippet.tags)
                .build();



    }

    @Data
    static class SearchApiResponse {
        private List<SearchItem> items;
    }

    @Data
    static class SearchItem {
        Id id;
    }

    @Data
    static class Id {
        String videoId;
    }

    @Data
    static class VideoApiResponse {
        private List<VideoItem> items;
    }

    @Data
    static class VideoItem {
        Snippet snippet;
    }

    @Data
    static class Snippet {
        private    String title;
        private String description;
        private String channelTitle;
        private String publishedAt;
        private List<String> tags;
        private Thumbnails thumbnails;
    }

    @Data
    static class Thumbnails {
        private ThumbnailInfo maxres;
        private ThumbnailInfo high;
        private ThumbnailInfo medium;
        private ThumbnailInfo _default;

        public String getBestThumbnailUrl() {
            if (maxres != null) return maxres.getUrl();
            if (high != null) return high.getUrl();
            if (medium != null) return medium.getUrl();
            return _default != null ? _default.getUrl() : "";
        }
    }

    @Data
    static class ThumbnailInfo {
        private String url;
        private Integer width;
        private Integer height;
    }
} */
package com.YouTube.Tools.MohdArmanTyagi.service;

import com.YouTube.Tools.MohdArmanTyagi.model.SearchVideo;
import com.YouTube.Tools.MohdArmanTyagi.model.Video;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class YouTubeService {
    private final WebClient youtubeWebClient;

    @Value("${youtube.api.key}")
    private String apiKey;

    @Value("${youtube.api.max.related.videos}")
    private int maxRelatedVideos;

    public SearchVideo searchVideos(String videoTitle) {
        log.info("Searching for videos with title: {}", videoTitle);

        List<String> videoIds = searchForVideoIds(videoTitle);
        if(videoIds.isEmpty()){
            log.warn("No video IDs found for title: {}", videoTitle);
            return SearchVideo.builder()
                    .primaryVideo(null)
                    .relatedVideo(Collections.emptyList())
                    .build();
        }

        String primaryVideoId = videoIds.get(0);
        List<String> relatedVideoIds = videoIds.subList(1, Math.min(videoIds.size(), maxRelatedVideos + 1));

        Video primaryVideo = getVideoById(primaryVideoId);
        List<Video> relatedVideos = new ArrayList<>();

        for (String id: relatedVideoIds){
            Video video = getVideoById(id);
            if(video != null){
                relatedVideos.add(video);
            }
        }

        log.info("Found {} related videos", relatedVideos.size());
        return SearchVideo.builder()
                .primaryVideo(primaryVideo)
                .relatedVideo(relatedVideos)
                .build();
    }

    private List<String> searchForVideoIds(String videoTitle) {
        try {
            log.debug("Searching YouTube for: {}", videoTitle);

            SearchApiResponse response = youtubeWebClient
                    .get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/search")
                            .queryParam("part", "snippet")
                            .queryParam("q", videoTitle)
                            .queryParam("type", "video")
                            .queryParam("maxResults", maxRelatedVideos + 1)
                            .queryParam("key", apiKey)
                            .build())
                    .retrieve()
                    .bodyToMono(SearchApiResponse.class)
                    .block();

            if(response == null || response.items == null) {
                log.warn("No response or items in search response");
                return Collections.emptyList();
            }

            List<String> videoIds = new ArrayList<>();
            for (SearchItem item : response.items) {
                if(item.id != null && item.id.videoId != null) {
                    videoIds.add(item.id.videoId);
                    log.debug("Found video ID: {}", item.id.videoId);
                }
            }

            log.info("Found {} video IDs", videoIds.size());
            return videoIds;

        } catch (WebClientResponseException e) {
            log.error("YouTube API search error: {} - {}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new RuntimeException("YouTube API error: " + e.getStatusCode() + " - " + e.getResponseBodyAsString());
        } catch (Exception e) {
            log.error("Unexpected error during search: {}", e.getMessage());
            throw new RuntimeException("Search failed: " + e.getMessage());
        }
    }

    public Video getVideoById(String videoId) {
        try {
            log.debug("Fetching video details for ID: {}", videoId);

            VideoApiResponse response = youtubeWebClient
                    .get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/videos")
                            .queryParam("part", "snippet")
                            .queryParam("id", videoId)
                            .queryParam("key", apiKey)
                            .build())
                    .retrieve()
                    .bodyToMono(VideoApiResponse.class)
                    .block();

            if (response == null || response.items == null || response.items.isEmpty()) {
                log.warn("No video found for ID: {}", videoId);
                return null;
            }

            Snippet snippet = response.items.get(0).snippet;

            // Get the best available thumbnail
            String thumbnailUrl = "";
            if (snippet.thumbnails != null) {
                thumbnailUrl = snippet.thumbnails.getBestThumbnailUrl();
            }

            Video video = Video.builder()
                    .id(videoId)
                    .channelTitle(snippet.channelTitle)
                    .title(snippet.title)
                    .description(snippet.description != null ? snippet.description : "")
                    .publishedAt(snippet.publishedAt != null ? snippet.publishedAt : "")
                    .thumbnailUrl(thumbnailUrl)
                    .tags(snippet.tags == null ? Collections.emptyList() : snippet.tags)
                    .build();

            log.debug("Found video: '{}' with {} tags", video.getTitle(), video.getTags().size());
            return video;

        } catch (WebClientResponseException e) {
            log.error("YouTube API video details error for {}: {} - {}", videoId, e.getStatusCode(), e.getResponseBodyAsString());
            return null;
        } catch (Exception e) {
            log.error("Unexpected error fetching video details for {}: {}", videoId, e.getMessage());
            return null;
        }
    }

    // Inner response classes
    @Data
    static class SearchApiResponse {
        private List<SearchItem> items;
    }

    @Data
    static class SearchItem {
        private Id id;
        private Snippet snippet;
    }

    @Data
    static class Id {
        private String videoId;
    }

    @Data
    static class VideoApiResponse {
        private List<VideoItem> items;
    }

    @Data
    static class VideoItem {
        private Snippet snippet;
    }

    @Data
    static class Snippet {
        private String title;
        private String description;
        private String channelTitle;
        private String publishedAt;
        private List<String> tags;
        private Thumbnails thumbnails;
    }

    @Data
    static class Thumbnails {
        private ThumbnailInfo maxres;
        private ThumbnailInfo high;
        private ThumbnailInfo medium;
        private ThumbnailInfo _default;

        public String getBestThumbnailUrl() {
            if (maxres != null && maxres.url != null) return maxres.url;
            if (high != null && high.url != null) return high.url;
            if (medium != null && medium.url != null) return medium.url;
            if (_default != null && _default.url != null) return _default.url;
            return "";
        }
    }

    @Data
    static class ThumbnailInfo {
        private String url;
        private Integer width;
        private Integer height;
    }
}