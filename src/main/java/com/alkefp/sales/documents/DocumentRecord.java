package com.alkefp.sales.documents;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record DocumentRecord(long id, String originalName, String mimeType, long sizeBytes,
        String driveFileId, LocalDate documentDate, int financialYear, int month,
        String uploadedBy, LocalDateTime uploadedAt) {
}
