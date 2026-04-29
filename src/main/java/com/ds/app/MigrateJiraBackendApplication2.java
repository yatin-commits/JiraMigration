package com.ds.app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.CommandLineRunner;
import com.ds.app.service.DownloadService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
//@SpringBootApplication
@RequiredArgsConstructor
public class MigrateJiraBackendApplication2 implements CommandLineRunner { 
	
	private final DownloadService downloadService;

	public static void main(String[] args) {
		SpringApplication.run(MigrateJiraBackendApplication2.class, args);
	}
	
	@Override
    public void run(String... args)  {
        log.info("========================================");
        log.info("  JIRA ATTACHMENT DOWNLOADER");
        log.info("========================================");
        downloadService.downloadAll();
    }

}
