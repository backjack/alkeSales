package com.alkefp.sales.documents;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Service
public class GoogleDriveDocumentService {
    private static final String DRIVE_SCOPE = "https://www.googleapis.com/auth/drive.file";
    private static final String FOLDER_MIME = "application/vnd.google-apps.folder";
    private final JdbcTemplate jdbc;
    private final GoogleDriveProperties properties;
    private final ObjectMapper mapper;
    private final HttpClient http = HttpClient.newBuilder().followRedirects(HttpClient.Redirect.NORMAL).build();

    public GoogleDriveDocumentService(JdbcTemplate jdbc, GoogleDriveProperties properties, ObjectMapper mapper) {
        this.jdbc = jdbc; this.properties = properties; this.mapper = mapper;
    }

    @PostConstruct
    void initializeTables() {
        jdbc.execute("create table if not exists google_drive_oauth (id int primary key, access_token text, refresh_token text, expires_at bigint, root_folder_id varchar(255), updated_at timestamp default current_timestamp)");
        jdbc.execute("create table if not exists uploaded_document (id bigint primary key auto_increment, original_name varchar(500) not null, mime_type varchar(150) not null, size_bytes bigint not null, drive_file_id varchar(255) not null unique, drive_folder_id varchar(255) not null, document_date date not null, financial_year int not null, document_month int not null, uploaded_by varchar(150) not null, uploaded_at timestamp default current_timestamp, index idx_document_period(financial_year, document_month))");
    }

    public boolean configured() { return properties.configured(); }
    public boolean connected() {
        Integer count = jdbc.queryForObject("select count(*) from google_drive_oauth where id=1 and refresh_token is not null", Integer.class);
        return count != null && count > 0;
    }

    public String authorizationUrl(String state) {
        requireConfigured();
        return "https://accounts.google.com/o/oauth2/v2/auth?client_id=" + enc(properties.getClientId())
                + "&redirect_uri=" + enc(properties.getRedirectUri()) + "&response_type=code&scope=" + enc(DRIVE_SCOPE)
                + "&access_type=offline&prompt=consent&state=" + enc(state);
    }

    public void exchangeAuthorizationCode(String code) {
        requireConfigured();
        JsonNode token = sendForm("code=" + enc(code) + "&client_id=" + enc(properties.getClientId())
                + "&client_secret=" + enc(properties.getClientSecret()) + "&redirect_uri=" + enc(properties.getRedirectUri())
                + "&grant_type=authorization_code");
        String refresh = token.path("refresh_token").asText("");
        if (refresh.isBlank()) refresh = jdbc.query("select refresh_token from google_drive_oauth where id=1", rs -> rs.next() ? rs.getString(1) : "");
        if (refresh == null || refresh.isBlank()) throw new IllegalStateException("Google did not return a refresh token");
        long expiry = Instant.now().getEpochSecond() + token.path("expires_in").asLong(3600) - 60;
        jdbc.update("insert into google_drive_oauth(id,access_token,refresh_token,expires_at,updated_at) values(1,?,?,?,current_timestamp) on duplicate key update access_token=values(access_token),refresh_token=values(refresh_token),expires_at=values(expires_at),updated_at=current_timestamp",
                token.path("access_token").asText(), refresh, expiry);
    }

    public List<DocumentRecord> upload(LocalDate date, MultipartFile[] files, String username) {
        if (!connected()) throw new IllegalStateException("Connect Google Drive before uploading files");
        if (files == null || files.length == 0) throw new IllegalArgumentException("Select at least one file");
        if (files.length > 20) throw new IllegalArgumentException("A maximum of 20 files can be uploaded together");
        int fy = financialYear(date);
        String fyFolder = findOrCreateFolder("FY-" + fy + "-" + (fy + 1), rootFolder());
        String monthFolder = findOrCreateFolder(date.format(DateTimeFormatter.ofPattern("MMM-yy", Locale.ENGLISH)), fyFolder);
        List<DocumentRecord> saved = new ArrayList<>();
        for (MultipartFile file : files) {
            validate(file);
            String fileId = uploadToDrive(file, monthFolder);
            jdbc.update("insert into uploaded_document(original_name,mime_type,size_bytes,drive_file_id,drive_folder_id,document_date,financial_year,document_month,uploaded_by) values(?,?,?,?,?,?,?,?,?)",
                    cleanName(file.getOriginalFilename()), contentType(file), file.getSize(), fileId, monthFolder, date, fy, date.getMonthValue(), username);
            saved.add(find(Objects.requireNonNull(jdbc.queryForObject("select last_insert_id()", Long.class))));
        }
        return saved;
    }

    public List<DocumentRecord> list(int fy, int month) {
        return jdbc.query("select id,original_name,mime_type,size_bytes,drive_file_id,document_date,financial_year,document_month,uploaded_by,uploaded_at from uploaded_document where financial_year=? and document_month=? order by uploaded_at desc", this::map, fy, month);
    }

    public DocumentRecord find(long id) {
        return jdbc.queryForObject("select id,original_name,mime_type,size_bytes,drive_file_id,document_date,financial_year,document_month,uploaded_by,uploaded_at from uploaded_document where id=?", this::map, id);
    }

    private DocumentRecord map(java.sql.ResultSet rs, int row) throws java.sql.SQLException {
        return new DocumentRecord(rs.getLong("id"), rs.getString("original_name"), rs.getString("mime_type"), rs.getLong("size_bytes"), rs.getString("drive_file_id"), rs.getDate("document_date").toLocalDate(), rs.getInt("financial_year"), rs.getInt("document_month"), rs.getString("uploaded_by"), rs.getTimestamp("uploaded_at").toLocalDateTime());
    }

    public byte[] download(long id) { return driveBytes(find(id).driveFileId()); }
    public byte[] monthZip(int fy, int month) {
        List<DocumentRecord> documents = list(fy, month);
        if (documents.isEmpty()) throw new IllegalArgumentException("No files exist for the selected month");
        try (ByteArrayOutputStream out = new ByteArrayOutputStream(); ZipOutputStream zip = new ZipOutputStream(out)) {
            int i = 1;
            for (DocumentRecord document : documents) {
                zip.putNextEntry(new ZipEntry(String.format("%02d-%s", i++, cleanName(document.originalName()))));
                zip.write(driveBytes(document.driveFileId())); zip.closeEntry();
            }
            zip.finish(); return out.toByteArray();
        } catch (IOException e) { throw new IllegalStateException("Could not create ZIP", e); }
    }

    public static int financialYear(LocalDate date) { return date.getMonthValue() >= 4 ? date.getYear() : date.getYear() - 1; }

    private String rootFolder() {
        if (!properties.getRootFolderId().isBlank()) return properties.getRootFolderId();
        String existing = jdbc.query("select root_folder_id from google_drive_oauth where id=1", rs -> rs.next() ? rs.getString(1) : null);
        if (existing != null && !existing.isBlank()) return existing;
        String created = findOrCreateFolder("ALKE Documents", null);
        jdbc.update("update google_drive_oauth set root_folder_id=? where id=1", created);
        return created;
    }

    private String findOrCreateFolder(String name, String parent) {
        String query = "name = '" + name.replace("'", "\\'") + "' and mimeType = '" + FOLDER_MIME + "' and trashed = false";
        if (parent != null) query += " and '" + parent + "' in parents";
        JsonNode response = sendJson("GET", URI.create("https://www.googleapis.com/drive/v3/files?q=" + enc(query) + "&fields=files(id)&spaces=drive&supportsAllDrives=true&includeItemsFromAllDrives=true"), null);
        if (!response.path("files").isEmpty()) return response.path("files").get(0).path("id").asText();
        String json = "{\"name\":" + json(name) + ",\"mimeType\":\"" + FOLDER_MIME + "\"" + (parent == null ? "" : ",\"parents\":[" + json(parent) + "]") + "}";
        return sendJson("POST", URI.create("https://www.googleapis.com/drive/v3/files?supportsAllDrives=true"), json).path("id").asText();
    }

    private String uploadToDrive(MultipartFile file, String parent) {
        String boundary = "alke-" + UUID.randomUUID();
        String metadata = "{\"name\":" + json(cleanName(file.getOriginalFilename())) + ",\"parents\":[" + json(parent) + "]}";
        try {
            ByteArrayOutputStream body = new ByteArrayOutputStream();
            body.write(("--" + boundary + "\r\nContent-Type: application/json; charset=UTF-8\r\n\r\n" + metadata + "\r\n--" + boundary + "\r\nContent-Type: " + contentType(file) + "\r\n\r\n").getBytes(StandardCharsets.UTF_8));
            body.write(file.getBytes()); body.write(("\r\n--" + boundary + "--\r\n").getBytes(StandardCharsets.UTF_8));
            HttpRequest request = authorized(URI.create("https://www.googleapis.com/upload/drive/v3/files?uploadType=multipart&supportsAllDrives=true"))
                    .header("Content-Type", "multipart/related; boundary=" + boundary).POST(HttpRequest.BodyPublishers.ofByteArray(body.toByteArray())).build();
            return parse(send(request)).path("id").asText();
        } catch (IOException e) { throw new IllegalStateException("Could not read uploaded file", e); }
    }

    private byte[] driveBytes(String id) {
        try {
            HttpResponse<byte[]> response = http.send(authorized(URI.create("https://www.googleapis.com/drive/v3/files/" + enc(id) + "?alt=media&supportsAllDrives=true")).GET().build(), HttpResponse.BodyHandlers.ofByteArray());
            if (response.statusCode() / 100 != 2) throw new IllegalStateException("Google Drive download failed: HTTP " + response.statusCode());
            return response.body();
        } catch (IOException e) { throw new IllegalStateException("Google Drive download failed", e); }
        catch (InterruptedException e) { Thread.currentThread().interrupt(); throw new IllegalStateException("Google Drive download interrupted", e); }
    }

    private JsonNode sendJson(String method, URI uri, String body) {
        HttpRequest.Builder builder = authorized(uri).header("Content-Type", "application/json");
        return parse(send("POST".equals(method) ? builder.POST(HttpRequest.BodyPublishers.ofString(body)).build() : builder.GET().build()));
    }
    private HttpRequest.Builder authorized(URI uri) { return HttpRequest.newBuilder(uri).header("Authorization", "Bearer " + accessToken()); }
    private String accessToken() {
        return jdbc.query("select access_token,refresh_token,expires_at from google_drive_oauth where id=1", rs -> {
            if (!rs.next()) throw new IllegalStateException("Google Drive is not connected");
            if (rs.getLong("expires_at") > Instant.now().getEpochSecond()) return rs.getString("access_token");
            JsonNode token = sendForm("client_id=" + enc(properties.getClientId()) + "&client_secret=" + enc(properties.getClientSecret()) + "&refresh_token=" + enc(rs.getString("refresh_token")) + "&grant_type=refresh_token");
            long expiry = Instant.now().getEpochSecond() + token.path("expires_in").asLong(3600) - 60;
            jdbc.update("update google_drive_oauth set access_token=?,expires_at=?,updated_at=current_timestamp where id=1", token.path("access_token").asText(), expiry);
            return token.path("access_token").asText();
        });
    }
    private JsonNode sendForm(String form) {
        HttpRequest request = HttpRequest.newBuilder(URI.create("https://oauth2.googleapis.com/token")).header("Content-Type", "application/x-www-form-urlencoded").POST(HttpRequest.BodyPublishers.ofString(form)).build();
        return parse(send(request));
    }
    private HttpResponse<String> send(HttpRequest request) {
        try {
            HttpResponse<String> response = http.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() / 100 != 2) throw new IllegalStateException("Google API request failed: " + response.body());
            return response;
        } catch (IOException e) { throw new IllegalStateException("Google API request failed", e); }
        catch (InterruptedException e) { Thread.currentThread().interrupt(); throw new IllegalStateException("Google API request interrupted", e); }
    }
    private JsonNode parse(HttpResponse<String> response) { try { return mapper.readTree(response.body()); } catch (IOException e) { throw new IllegalStateException("Invalid response from Google", e); } }
    private void validate(MultipartFile file) {
        if (file.isEmpty()) throw new IllegalArgumentException("Empty files cannot be uploaded");
        if (file.getSize() > 25L * 1024 * 1024) throw new IllegalArgumentException(cleanName(file.getOriginalFilename()) + " exceeds 25 MB");
        if (!Set.of("application/pdf", "image/jpeg", "image/png", "image/webp").contains(contentType(file))) throw new IllegalArgumentException("Only PDF, JPG, PNG and WebP files are supported");
    }
    private void requireConfigured() { if (!configured()) throw new IllegalStateException("Google Drive OAuth is not configured"); }
    private String contentType(MultipartFile file) { return Optional.ofNullable(file.getContentType()).orElse("application/octet-stream").toLowerCase(); }
    private String cleanName(String name) { return Optional.ofNullable(name).orElse("document").replaceAll("[\\r\\n/\\\\]", "_"); }
    private String json(String value) { try { return mapper.writeValueAsString(value); } catch (Exception e) { throw new IllegalStateException(e); } }
    private static String enc(String value) { return URLEncoder.encode(value, StandardCharsets.UTF_8); }
}
