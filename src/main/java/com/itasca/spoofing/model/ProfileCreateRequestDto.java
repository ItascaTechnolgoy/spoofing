package com.itasca.spoofing.model;


import lombok.*;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
@EqualsAndHashCode
public class ProfileCreateRequestDto {

    @JsonProperty("profile_type")
    @NotNull(message = "Profile type is required")
    private ProfileType profileType;

    @JsonProperty("single_profile")
    @Valid
    private SingleProfileDto singleProfile;

    @JsonProperty("group_profile")
    @Valid
    private GroupProfileDto groupProfile;

    /**
     * Validate that the correct profile data is provided based on profile type
     */
    public boolean isValid() {
        if (profileType == ProfileType.SINGLE) {
            return singleProfile != null && groupProfile == null;
        } else if (profileType == ProfileType.GROUP) {
            return groupProfile != null && singleProfile == null;
        }
        return false;
    }
}