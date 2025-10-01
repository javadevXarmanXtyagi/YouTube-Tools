package com.YouTube.Tools.MohdArmanTyagi.model;

import lombok.Builder;
import lombok.Data;
import java.util.List;

@Data
@Builder
public class Video {
    private String id;
    private String title;
    private String channelTitle;
    private List<String> tags;
    private String description;
    private String publishedAt;
    private String thumbnailUrl;

    public String getTagsAsString() {
        if (tags == null || tags.isEmpty()) {
            return "";
        }
        return String.join(", ", tags);
    }
}