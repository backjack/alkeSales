package com.alkefp.sales.documents;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "google.drive")
public class GoogleDriveProperties {
    private String serviceAccountJsonBase64 = "";
    private String rootFolderId = "";

    public String getServiceAccountJsonBase64() { return serviceAccountJsonBase64; }
    public void setServiceAccountJsonBase64(String serviceAccountJsonBase64) { this.serviceAccountJsonBase64 = serviceAccountJsonBase64; }
    public String getRootFolderId() { return rootFolderId; }
    public void setRootFolderId(String rootFolderId) { this.rootFolderId = rootFolderId; }
    public boolean configured() { return !serviceAccountJsonBase64.isBlank() && !rootFolderId.isBlank(); }
}
