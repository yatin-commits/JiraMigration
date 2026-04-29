package com.ds.app.service;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.ds.app.model.AttachmentEntry;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@Service
@RequiredArgsConstructor
public class DownloadService {

    private final CsvReaderService csvReaderService;
    private final RestTemplate restTemplate;

    @Value("${jira.test-limit:0}")
    private int testLimit;  // 0 = no limit, >0 = test mode

    @Value("${jira.download-dir}")
    private String downloadDir;

    @Value("${jira.download-threads:10}")  // bumped from 3 → 10
    private int threads;

    @Value("${jira.skip-existing:true}")
    private boolean skipExisting;

    @Value("${jira.request-delay-ms:100}")  // 100ms delay = ~10 req/sec safe rate
    private int requestDelayMs;

    // Progress counters
    private final AtomicInteger downloaded = new AtomicInteger(0);
    private final AtomicInteger skipped    = new AtomicInteger(0);
    private final AtomicInteger errors     = new AtomicInteger(0);

    public void downloadAll() {
        log.info("Reading CSV...");
        List<AttachmentEntry> attachments = csvReaderService.readAttachments();

        if (attachments.isEmpty()) {
            log.warn("No attachments found in CSV. Exiting.");
            return;
        }
        log.info("Total attachments to process: {}", attachments.size());
        log.info("Download directory: {}", downloadDir);
        log.info("Parallel threads: {}", threads);
        log.info("Request delay: {}ms", requestDelayMs);
        log.info("Skip existing: {}", skipExisting);
        log.info("----------------------------------------");

        ExecutorService executor = Executors.newFixedThreadPool(threads);

        int limit = testLimit > 0 ? Math.min(testLimit, attachments.size()) : attachments.size();
        log.info("Downloading {} attachments", limit);

        for (int i = 0; i < limit; i++) {
            AttachmentEntry entry = attachments.get(i);
            executor.submit(() -> processAttachment(entry));
        }

        executor.shutdown();
        try {
            executor.awaitTermination(12, TimeUnit.HOURS);
        } catch (InterruptedException e) {
            log.error("Download interrupted!", e);
            Thread.currentThread().interrupt();
        }

        log.info("========================================");
        log.info("  DOWNLOAD COMPLETE");
        log.info("========================================");
        log.info("  Downloaded : {}", downloaded.get());
        log.info("  Skipped    : {} (already existed)", skipped.get());
        log.info("  Errors     : {}", errors.get());
        log.info("  Total      : {}", attachments.size());
        log.info("========================================");
    }

    private void processAttachment(AttachmentEntry entry) {
        int maxRetries   = 3;
        int retryDelayMs = 5000;

        // Rate limiting — 100ms delay keeps us at ~10 req/sec across all threads
        if (requestDelayMs > 0) {
            try { Thread.sleep(requestDelayMs); }
            catch (InterruptedException ie) { Thread.currentThread().interrupt(); }
        }

        for (int attempt = 1; attempt <= maxRetries; attempt++) {
            try {
                Path destPath = buildDestPath(entry);

                if (skipExisting && Files.exists(destPath)) {
                    int s = skipped.incrementAndGet();
                    if (s % 500 == 0) log.info("Skipped so far: {}", s);
                    return;
                }

                Files.createDirectories(destPath.getParent());

                ResponseEntity<byte[]> response = restTemplate.getForEntity(entry.getUrl(), byte[].class);

                if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                    Files.write(destPath, response.getBody());
                    int d = downloaded.incrementAndGet();
                    if (d % 100 == 0) {
                        log.info("Progress: downloaded={}, skipped={}, errors={}",
                                d, skipped.get(), errors.get());
                    }
                    return;

                } else if (response.getStatusCode().value() == 429) {
                    log.warn("Rate limit [{}/{}]: {} | waiting {}ms",
                            attempt, maxRetries, entry.getIssueKey(), retryDelayMs);
                    Thread.sleep(retryDelayMs);
                    retryDelayMs *= 2;

                } else {
                    log.warn("FAILED [{}]: {} | {}",
                            response.getStatusCode(), entry.getIssueKey(), entry.getFilename());
                    errors.incrementAndGet();
                    return;
                }

            } catch (Exception e) {
                log.error("ERROR [{}/{}]: {} | {} | {} | {}",
                        attempt, maxRetries,
                        entry.getIssueKey(),
                        entry.getFilename(),
                        e.getClass().getSimpleName(),
                        e.getMessage());

                if (attempt == maxRetries) {
                    errors.incrementAndGet();
                } else {
                    try { Thread.sleep(retryDelayMs); }
                    catch (InterruptedException ie) { Thread.currentThread().interrupt(); }
                    retryDelayMs *= 2;
                }
            }
        }

        log.error("GIVING UP: {} | {}", entry.getIssueKey(), entry.getFilename());
    }

    /**
     * Hierarchy logic — 3 cases:
     *
     *   CASE 1 — Epic (no parent, issueType = "Epic"):
     *     EPICS/EPIC-KEY/_epic_attachments/filename
     *
     *   CASE 2 — Child issue (has parentKey):
     *     EPICS/PARENT-KEY/ISSUE-KEY/filename
     *
     *   CASE 3 — Flat/orphan issue (no parent, not an Epic):
     *     NO_PARENT/ISSUE-KEY/filename
     */
    private Path buildDestPath(AttachmentEntry entry) {
        String parentKey  = entry.getParentKey();
        String issueType  = entry.getIssueType();   
        String issueKey   = sanitize(entry.getIssueKey());
        String filename   = sanitize(entry.getFilename());

        boolean hasParent = parentKey != null && !parentKey.isBlank();
        boolean isEpic    = "Epic".equalsIgnoreCase(issueType);

        if (isEpic) {
            // Epic's own attachments go inside EPICS/EPIC-KEY/_epic_attachments/
            return Paths.get(downloadDir, "EPICS", issueKey, "_epic_attachments", filename);

        } else if (hasParent) {
            // Child issue → nested inside its parent epic folder
            return Paths.get(downloadDir, "EPICS", sanitize(parentKey), issueKey, filename);

        } else {
            // Flat/orphan — no parent, not an epic
            return Paths.get(downloadDir, "NO_PARENT", issueKey, filename);
        }
    }

    /**
     * Strip invalid characters for both Windows and Linux
     */
    private String sanitize(String name) {
        return name.replaceAll("[\\\\/*?:\"<>|]", "_").trim();
    }
}