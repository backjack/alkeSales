package com.alkefp.sales.controller;

import com.alkefp.sales.beans.BaseResponse;
import com.alkefp.sales.documents.DocumentRecord;
import com.alkefp.sales.documents.GoogleDriveDocumentService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.*;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.net.URI;
import java.time.LocalDate;
import java.time.Month;
import java.time.format.TextStyle;
import java.util.*;

@RestController
@RequestMapping("/documents")
public class DocumentController {
    private final GoogleDriveDocumentService service;
    public DocumentController(GoogleDriveDocumentService service) { this.service = service; }

    @GetMapping("/google/status")
    public BaseResponse<Map<String, Boolean>> status() {
        return response(Map.of("configured", service.configured(), "connected", service.connected()));
    }

    @GetMapping("/google/connect")
    public void connect(HttpSession session, HttpServletResponse response) throws java.io.IOException {
        String state = UUID.randomUUID().toString();
        session.setAttribute("googleDriveOauthState", state);
        response.sendRedirect(service.authorizationUrl(state));
    }

    @GetMapping("/google/callback")
    public ResponseEntity<Void> callback(@RequestParam String code, @RequestParam String state, HttpSession session) {
        Object expected = session.getAttribute("googleDriveOauthState");
        if (expected == null || !expected.equals(state)) return ResponseEntity.badRequest().build();
        session.removeAttribute("googleDriveOauthState");
        service.exchangeAuthorizationCode(code);
        return ResponseEntity.status(HttpStatus.FOUND).location(URI.create("/documents?google=connected")).build();
    }

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public BaseResponse<List<DocumentRecord>> upload(@RequestParam LocalDate documentDate,
            @RequestPart("files") MultipartFile[] files, Authentication authentication) {
        return response(service.upload(documentDate, files, authentication.getName()));
    }

    @GetMapping("/list")
    public BaseResponse<List<DocumentRecord>> list(@RequestParam int financialYear, @RequestParam int month) {
        return response(service.list(financialYear, month));
    }

    @GetMapping("/{id}/download")
    public ResponseEntity<byte[]> download(@PathVariable long id) {
        DocumentRecord document = service.find(id);
        return ResponseEntity.ok().contentType(MediaType.parseMediaType(document.mimeType()))
                .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment().filename(document.originalName()).build().toString())
                .body(service.download(id));
    }

    @GetMapping("/month.zip")
    public ResponseEntity<byte[]> monthZip(@RequestParam int financialYear, @RequestParam int month) {
        String monthName = Month.of(month).getDisplayName(TextStyle.SHORT, Locale.ENGLISH);
        String filename = "FY-" + financialYear + "-" + (financialYear + 1) + "-" + monthName + ".zip";
        return ResponseEntity.ok().contentType(MediaType.parseMediaType("application/zip"))
                .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment().filename(filename).build().toString())
                .body(service.monthZip(financialYear, month));
    }

    private static <T> BaseResponse<T> response(T data) {
        BaseResponse<T> result = new BaseResponse<>(); result.setSuccess(true); result.setData(data); return result;
    }
}
