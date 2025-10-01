package com.YouTube.Tools.MohdArmanTyagi.model;

import lombok.Builder;
import lombok.Data;
import java.util.List;
import java.util.stream.Collectors;

@Data
@Builder
public class SearchVideo {
    private Video primaryVideo;
    private List<Video> relatedVideo;

    // Add this method to get all tags as string
    public String getAllTagsAsString() {
        StringBuilder allTags = new StringBuilder();

        if (primaryVideo != null && primaryVideo.getTags() != null) {
            allTags.append(String.join(", ", primaryVideo.getTags()));
        }

        if (relatedVideo != null) {
            for (Video video : relatedVideo) {
                if (video.getTags() != null && !video.getTags().isEmpty()) {
                    if (allTags.length() > 0) {
                        allTags.append(", ");
                    }
                    allTags.append(String.join(", ", video.getTags()));
                }
            }
        }

        return allTags.toString();
    }
}