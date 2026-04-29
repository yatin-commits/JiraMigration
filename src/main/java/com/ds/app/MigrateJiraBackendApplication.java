package com.ds.app;

import com.ds.app.service.CsvImportService;
import com.ds.app.service.DownloadService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@Slf4j
@SpringBootApplication
@RequiredArgsConstructor
//public class MigrateJiraBackendApplication implements CommandLineRunner {
public class MigrateJiraBackendApplication  {
    private final DownloadService downloadService;
    private final CsvImportService csvImportService;
    @Value("${app.mode}")
    private String mode;

    public static void main(String[] args) {
        SpringApplication.run(MigrateJiraBackendApplication.class, args);
    }

//    @Override
//    public void run(String... args) {
//        log.info("Running in mode: {}", mode);
//        switch (mode.toLowerCase()) {
//            case "download" -> downloadService.downloadAll();
//            case "import"   -> csvImportService.importAll();
//            case "both"     -> { downloadService.downloadAll(); csvImportService.importAll(); }
//            default         -> log.warn("Unknown mode: {}. Use: download | import | both", mode);
//        }
//    }
}