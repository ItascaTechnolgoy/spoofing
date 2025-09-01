package com.itasca.spoofing.model;


import lombok.*;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = {"password"})
@EqualsAndHashCode(exclude = {"password"})
public class ProxyConfig {

    @JsonProperty("proxy_type")
    @Builder.Default
    private String proxyType = "None";

    @Builder.Default
    private String host = "";

    @Min(value = 1, message = "Port must be between 1 and 65535")
    @Max(value = 65535, message = "Port must be between 1 and 65535")
    @Builder.Default
    private Integer port = 8080;

    @JsonProperty("end_port")
    @Min(value = 1, message = "End port must be between 1 and 65535")
    @Max(value = 65535, message = "End port must be between 1 and 65535")
    private Integer endPort;

    @Builder.Default
    private String username = "";

    @Builder.Default
    private String password = "";

    @JsonProperty("ip_type")
    @Builder.Default
    private IPType ipType = IPType.RANDOM;

    @JsonProperty("fixed_ip")
    @Pattern(regexp = "^(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$|^$",
            message = "Invalid IP address format")
    private String fixedIp;

    @Builder.Default
    private String country = "US";

    /**
     * Check if proxy is configured (not None type and has host)
     */
    public boolean isProxyConfigured() {
        return !"None".equalsIgnoreCase(proxyType) &&
                host != null &&
                !host.trim().isEmpty();
    }

    /**
     * Check if proxy requires authentication
     */
    public boolean requiresAuthentication() {
        return username != null && !username.trim().isEmpty();
    }
}