package com.itasca.spoofing.model;


import lombok.*;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.util.ArrayList;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
@EqualsAndHashCode
public class URLGroupDto {

    @NotBlank(message = "URL group name is required")
    private String name;

    @NotNull(message = "URLs list cannot be null")
    @Builder.Default
    private List<String> urls = new ArrayList<>();

    @JsonProperty("current_index")
    @Builder.Default
    private Integer currentIndex = 0;

    @JsonProperty("completed_urls")
    @Builder.Default
    private List<String> completedUrls = new ArrayList<>();

    // Business logic methods (can be moved to service layer if needed)

    /**
     * Get the next URL from the list
     */
    public String getNextUrl() {
        if (currentIndex < urls.size()) {
            String url = urls.get(currentIndex);
            currentIndex++;
            completedUrls.add(url);
            return url;
        }
        return null;
    }

    /**
     * Check if there are remaining URLs
     */
    public boolean hasRemainingUrls() {
        return currentIndex < urls.size();
    }

    /**
     * Reset URL iteration
     */
    public void reset() {
        currentIndex = 0;
        completedUrls.clear();
    }

    /**
     * Get remaining URLs for manual selection
     */
    public List<String> getAvailableUrls() {
        if (currentIndex >= urls.size()) {
            return new ArrayList<>();
        }
        return urls.subList(currentIndex, urls.size());
    }

    /**
     * Manually select a specific URL
     */
    public boolean selectUrlManually(String url) {
        List<String> availableUrls = getAvailableUrls();
        if (availableUrls.contains(url)) {
            int urlIndex = urls.indexOf(url);
            if (urlIndex >= currentIndex) {
                // Swap the selected URL to current position
                String temp = urls.get(currentIndex);
                urls.set(currentIndex, url);
                urls.set(urlIndex, temp);

                currentIndex++;
                completedUrls.add(url);
                return true;
            }
        }
        return false;
    }
}