package com.ds.app.controller;

import com.ds.app.DTO.*;
import com.ds.app.service.IssueService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class IssueController {

    private final IssueService issueService;

    //  GET /api/projects
    @GetMapping("/projects")
    public ResponseEntity<List<ProjectDto>> getProjects() {
        return ResponseEntity.ok(issueService.getProjects());
    }

    //  GET /api/issue-types
    @GetMapping("/issue-types")
    public ResponseEntity<List<String>> getIssueTypes() {
        return ResponseEntity.ok(issueService.getIssueTypes());
    }

    //  GET /api/issue-keys?query=WS
    @GetMapping("/issue-keys")
    public ResponseEntity<List<String>> getIssueKeys(
            @RequestParam(required = false, defaultValue = "") String query) {
        return ResponseEntity.ok(issueService.getIssueKeys(query));
    }

    //  GET /api/issues
    @GetMapping("/issues")
    public ResponseEntity<PagedResponse<IssueListDto>> getIssues(
            @RequestParam(required = false) String projectKey,
            @RequestParam(required = false) String issueType,
            @RequestParam(required = false) String issueKey,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String prioritySort,
            @RequestParam(defaultValue = "1")  int page,
            @RequestParam(defaultValue = "20") int pageSize) {

        return ResponseEntity.ok(
                issueService.getIssues(projectKey, issueType, issueKey,
                        search, prioritySort, page, pageSize));
    }

    //   /api/issues/{issueKey}
    @GetMapping("/issues/{issueKey}")
    public ResponseEntity<IssueDetailDto> getIssueDetail(@PathVariable String issueKey) {
        IssueDetailDto detail = issueService.getIssueDetail(issueKey);
        if (detail == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(detail);
    }

    //  /api/issues/{issueKey}/children
    @GetMapping("/issues/{issueKey}/children")
    public ResponseEntity<List<IssueListDto>> getChildren(@PathVariable String issueKey) {
        return ResponseEntity.ok(issueService.getChildren(issueKey));
    }

    //  /api/attachments/{id}/download
    @GetMapping("/attachments/{id}/download")
    public ResponseEntity<Resource> downloadAttachment(@PathVariable Long id) {
        return issueService.getAttachmentDownload(id);
    }
}