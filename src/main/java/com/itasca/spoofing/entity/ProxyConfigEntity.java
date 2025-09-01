package com.itasca.spoofing.entity;


import lombok.*;
import com.itasca.spoofing.model.IPType;

import jakarta.persistence.*;

@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = {"password"}) // Exclude password from toString for security
@EqualsAndHashCode(exclude = {"password"})
public class ProxyConfigEntity {

    @Column(name = "proxy_type", length = 20)
    @Builder.Default
    private String proxyType = "None";

    @Column(name = "proxy_host", length = 255)
    @Builder.Default
    private String host = "";

    @Column(name = "proxy_port")
    @Builder.Default
    private Integer port = 8080;

    @Column(name = "proxy_end_port")
    private Integer endPort;

    @Column(name = "proxy_username", length = 255)
    @Builder.Default
    private String username = "";

    @Column(name = "proxy_password", length = 500)
    @Builder.Default
    private String password = "";

    @Column(name = "ip_type", length = 20)
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private IPType ipType = IPType.RANDOM;

    @Column(name = "fixed_ip", length = 15)
    private String fixedIp;

    @Column(name = "proxy_country", length = 2)
    @Builder.Default
    private String country = "US";

    /**
     * Check if proxy is configured
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