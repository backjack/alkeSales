package com.alkefp.sales.documents;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "google.drive")
public class GoogleDriveProperties {
    private String clientId = "";
    private String clientSecret = "";
    private String redirectUri = "http://localhost:8080/documents/google/callback";
    private String rootFolderId = "";

    public String getClientId() { return clientId; }
    public void setClientId(String clientId) { this.clientId = clientId; }
    public String getClientSecret() { return clientSecret; }
    public void setClientSecret(String clientSecret) { this.clientSecret = clientSecret; }
    public String getRedirectUri() { return redirectUri; }
    public void setRedirectUri(String redirectUri) { this.redirectUri = redirectUri; }
    public String getRootFolderId() { return rootFolderId; }
    public void setRootFolderId(String rootFolderId) { this.rootFolderId = rootFolderId; }
    public boolean configured() { return !clientId.isBlank() && !clientSecret.isBlank() && !redirectUri.isBlank(); }
}
