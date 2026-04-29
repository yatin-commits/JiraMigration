package com.ds.app.service;

import com.ds.app.model.AttachmentEntry;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.ICSVParser;
import com.opencsv.RFC4180ParserBuilder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.FileReader;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.regex.Pattern;

@Slf4j
@Service
public class CsvReaderService {

    @Value("${jira.csv-path}")
    private String csvPath;

    private static final Pattern ISSUE_KEY_PATTERN =
            Pattern.compile("^[A-Za-z][A-Za-z0-9]+-[0-9]+$");

    public List<AttachmentEntry> readAttachments() {
        List<AttachmentEntry> entries = new ArrayList<>();

        try (CSVReader reader = buildReader()) {
            String[] headers = reader.readNext();

            if (headers == null) {
                log.error("CSV is empty!");
                return entries;
            }

            for (int i = 0; i < headers.length; i++) {
                if (headers[i] != null) {
                    headers[i] = headers[i].replace("\uFEFF", "").trim();
                }
            }

            int issueKeyIdx  = findIndexIgnoreCase(headers, "Issue key");
            int parentKeyIdx = findIndexIgnoreCase(headers, "Parent key");
            int issueTypeIdx = findIndexIgnoreCase(headers, "Issue Type"); // ← ADDED
            List<Integer> attachmentIdxs = findAllIndexesIgnoreCase(headers, "Attachment");

            if (issueKeyIdx == -1) {
                log.error("'Issue key' column not found in CSV! Headers found: {}",
                        Arrays.toString(Arrays.copyOf(headers, Math.min(headers.length, 10))));
                return entries;
            }

            if (issueTypeIdx == -1) {
                log.warn("'Issue Type' column not found — Epic hierarchy will not work correctly!");
            }

            if (attachmentIdxs.isEmpty()) {
                log.warn("No 'Attachment' columns found in CSV.");
            }

            String lastIssueKey  = null;
            String lastParentKey = null;
            String lastIssueType = null; 
            String currentIssueBlock = null;
            Set<String> seenAttachmentsForCurrentIssue = new LinkedHashSet<>();

            String[] row;
            int rowNum = 1;
            int invalidIssueRows = 0;

            while ((row = reader.readNext()) != null) {
                rowNum++;

                String rawIssueKey  = getCellSafe(row, issueKeyIdx).trim();
                String rawParentKey = getCellSafe(row, parentKeyIdx).trim();
                String rawIssueType = getCellSafe(row, issueTypeIdx).trim(); // ← ADDED

                if (!rawIssueKey.isEmpty()) {
                    if (!ISSUE_KEY_PATTERN.matcher(rawIssueKey).matches()) {
                        invalidIssueRows++;
                        continue;
                    }

                    lastIssueKey  = rawIssueKey;
                    lastParentKey = rawParentKey.isEmpty() ? null : rawParentKey;
                    lastIssueType = rawIssueType.isEmpty() ? null : rawIssueType; // ← ADDED
                } else {
                    if (lastIssueKey == null) continue;
                }

                String issueKey  = lastIssueKey;
                String parentKey = lastParentKey;
                String issueType = lastIssueType; 

                if (!Objects.equals(currentIssueBlock, issueKey)) {
                    currentIssueBlock = issueKey;
                    seenAttachmentsForCurrentIssue.clear();
                }

                for (int idx : attachmentIdxs) {
                    String raw = getCellSafe(row, idx).trim();
                    if (raw.isEmpty()) continue;

                    String fp = issueKey + "||" + raw;
                    if (!seenAttachmentsForCurrentIssue.add(fp)) continue;

                    try {
                        AttachmentEntry entry = parseAttachment(raw, issueKey, parentKey, issueType); // ← ADDED
                        if (entry != null) entries.add(entry);
                    } catch (Exception e) {
                        log.warn("Row {}: exception parsing attachment: {}", rowNum, e.getMessage());
                    }
                }
            }

            if (invalidIssueRows > 0) {
                log.warn("Skipped {} rows with invalid issue keys", invalidIssueRows);
            }
            log.info("Total attachments parsed: {}", entries.size());

        } catch (Exception e) {
            log.error("Error reading CSV: {}", e.getMessage(), e);
        }

        return entries;
    }

    private AttachmentEntry parseAttachment(String raw, String issueKey, String parentKey, String issueType) { // ← ADDED
        if (raw == null || raw.isBlank()) return null;

        String normalized = raw.trim();

        if (normalized.startsWith("{")
                || normalized.startsWith("\"content\"")
                || (normalized.toUpperCase().equals(normalized) && !normalized.contains("http"))) {
            return null;
        }

        String[] parts = normalized.split(";", -1);
        if (parts.length < 4) return null;

        String uploadedAt = parts[0].trim();
        String userId     = parts[1].trim();

        // Find URL from end — supports filenames containing semicolons
        String url = null;
        int urlIndex = -1;
        for (int i = parts.length - 1; i >= 2; i--) {
            if (parts[i].trim().startsWith("http")) {
                url = parts[i].trim();
                urlIndex = i;
                break;
            }
        }

        if (url == null) return null;

        StringBuilder filenameSb = new StringBuilder();
        for (int i = 2; i < urlIndex; i++) {
            if (i > 2) filenameSb.append(";");
            filenameSb.append(parts[i].trim());
        }

        String filename = filenameSb.toString().trim();
        if (filename.isEmpty()) return null;

        return AttachmentEntry.builder()
                .issueKey(issueKey)
                .parentKey(parentKey)
                .issueType(issueType)   
                .uploadedAt(uploadedAt)
                .userId(userId)
                .filename(filename)
                .url(url)
                .build();
    }

    private int findIndexIgnoreCase(String[] headers, String name) {
        for (int i = 0; i < headers.length; i++) {
            if (headers[i] != null && headers[i].trim().equalsIgnoreCase(name)) return i;
        }
        return -1;
    }

    private List<Integer> findAllIndexesIgnoreCase(String[] headers, String name) {
        List<Integer> indexes = new ArrayList<>();
        for (int i = 0; i < headers.length; i++) {
            if (headers[i] != null && headers[i].trim().equalsIgnoreCase(name)) indexes.add(i);
        }
        return indexes;
    }

    private CSVReader buildReader() throws Exception {
        FileReader fileReader = new FileReader(csvPath, StandardCharsets.UTF_8);
        ICSVParser parser = new RFC4180ParserBuilder()
                .withSeparator(',')
                .withQuoteChar('"')
                .build();
        return new CSVReaderBuilder(fileReader)
                .withCSVParser(parser)
                .build();
    }

    private String getCellSafe(String[] row, int idx) {
        if (idx < 0 || idx >= row.length) return "";
        return row[idx] == null ? "" : row[idx];
    }
}