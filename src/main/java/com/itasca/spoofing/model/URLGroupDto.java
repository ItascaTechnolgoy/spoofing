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

    private Long id;

    @NotBlank(message = "URL group name is required")
    private String name;

    @NotNull(message = "URLs list cannot be null")
    @Builder.Default
    private List<URLDto> urls = new ArrayList<>();

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
            String url = urls.get(currentIndex).getUrl();
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
    public List<String> getRemainingUrls() {
        if (currentIndex >= urls.size()) {
            return new ArrayList<>();
        }
        return urls.subList(currentIndex, urls.size()).stream()
                .map(URLDto::getUrl)
                .collect(java.util.stream.Collectors.toList());
    }

    /**
     * Manually select a specific URL
     */
    public boolean selectUrlManually(String url) {
        List<String> remainingUrls = getRemainingUrls();
        if (remainingUrls.contains(url)) {
            int urlIndex = -1;
            for (int i = 0; i < urls.size(); i++) {
                if (urls.get(i).getUrl().equals(url)) {
                    urlIndex = i;
                    break;
                }
            }
            if (urlIndex >= currentIndex) {
                // Swap the selected URL to current position
                URLDto temp = urls.get(currentIndex);
                urls.set(currentIndex, urls.get(urlIndex));
                urls.set(urlIndex, temp);

                currentIndex++;
                completedUrls.add(url);
                return true;
            }
        }
        return false;
    }
}