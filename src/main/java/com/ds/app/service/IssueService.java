package com.ds.app.service;

import com.ds.app.DTO.*;
import com.ds.app.entity.JiraAttachment;
import com.ds.app.entity.JiraComment;
import com.ds.app.entity.JiraIssue;
import com.ds.app.repository.JiraAttachmentRepository;
import com.ds.app.repository.JiraCommentRepository;
import com.ds.app.repository.JiraIssueRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class IssueService {

    private final JiraIssueRepository      issueRepository;
    private final JiraAttachmentRepository attachmentRepository;
    private final JiraCommentRepository    commentRepository;

    @Value("${jira.download-dir}")
    private String downloadDir;

    private static final Map<String, Integer> PRIORITY_ORDER = Map.of(
        "Highest", 1,
        "High",    2,
        "Medium",  3,
        "Low",     4,
        "Lowest",  5
    );

    
    public List<ProjectDto> getProjects() {
        List<ProjectDto> projects = new ArrayList<>();

        List<Object[]> rows = issueRepository.findDistinctProjects();
        for (Object[] row : rows) {
            String projectKey = (String) row[0];
            String projectName = (String) row[1];

            projects.add(new ProjectDto(projectKey, projectName));
        }

        return projects;
    }


    //  Issue Types list
    public List<String> getIssueTypes() {
        return issueRepository.findDistinctIssueTypes();
    }

    //  Issue Keys autocomplete
    public List<String> getIssueKeys(String query) {
        if (query == null || query.isBlank()) return Collections.emptyList();
        return issueRepository.findIssueKeysByQuery(
                query.trim().toUpperCase(), PageRequest.of(0, 10));
    }

    //  Issues list with filters + pagination
    public PagedResponse<IssueListDto> getIssues(
            String projectKey, String issueType, String issueKey,
            String search, String prioritySort, int page, int pageSize) {

        Pageable pageable = PageRequest.of(Math.max(0, page - 1), pageSize);

        Page<JiraIssue> result = issueRepository.findWithFilters(
                projectKey, issueType, issueKey, search, pageable);

        List<IssueListDto> data = result.getContent()
                .stream()
                .map(this::toListDto)
                .collect(Collectors.toList());

        if ("asc".equalsIgnoreCase(prioritySort)) {
            Collections.sort(data, (a, b) -> {
                int p1 = PRIORITY_ORDER.getOrDefault(a.getPriority(), 99);
                int p2 = PRIORITY_ORDER.getOrDefault(b.getPriority(), 99);
                return p1 - p2;   // High → Low
            });
        } 
        else if ("desc".equalsIgnoreCase(prioritySort)) {
            Collections.sort(data, (a, b) -> {
                int p1 = PRIORITY_ORDER.getOrDefault(a.getPriority(), 99);
                int p2 = PRIORITY_ORDER.getOrDefault(b.getPriority(), 99);
                return p2 - p1;   // Low → High
            });
        }

        return new PagedResponse<>(data, page, pageSize, result.getTotalElements());
    }

    // 5. Issue detail
    public IssueDetailDto getIssueDetail(String issueKey) {
        JiraIssue issue = issueRepository.findByIssueKey(issueKey);
        if (issue == null) return null;

        List<AttachmentDto> attachments = new ArrayList<>();

        List<JiraAttachment> attachmentEntities =
                attachmentRepository.findByIssueKey(issueKey);

        for (JiraAttachment entity : attachmentEntities) {
            attachments.add(toAttachmentDto(entity));
        }

        
        List<CommentDto> comments = commentRepository
                .findByIssueKeyOrderByDate(issueKey)
                .stream()
                .map(this::toCommentDto)
                .collect(Collectors.toList());

        return toDetailDto(issue, attachments, comments);
    }

    
    public ResponseEntity<Resource> getAttachmentDownload(Long id) {
        Optional<JiraAttachment> opt = attachmentRepository.findById(id);
        if (opt.isEmpty()) return ResponseEntity.notFound().build();

        JiraAttachment att = opt.get();

       
        if (att.getFtpPath().startsWith("http")) {
            return ResponseEntity.status(302)
                    .header("Location", att.getFtpPath())
                    .build();
        }

        String relativePath = att.getFtpPath().startsWith("/")
                ? att.getFtpPath().substring(1)
                : att.getFtpPath();

        Path filePath = Paths.get(downloadDir, relativePath);

        if (!Files.exists(filePath)) {
            log.warn("File not found on disk: {}", filePath);
            return ResponseEntity.notFound().build();
        }

        try {
            Resource resource = new FileSystemResource(filePath);
            String mimeType   = Files.probeContentType(filePath);

           
            String encodedFilename = URLEncoder
                    .encode(att.getFilename(), StandardCharsets.UTF_8)
                    .replace("+", "%20");

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            "attachment; filename=\"" + encodedFilename + "\"; " +
                            "filename*=UTF-8''" + encodedFilename)  
                    .contentType(MediaType.parseMediaType(
                            mimeType != null ? mimeType : "application/octet-stream"))
                    .body(resource);

        } catch (Exception e) {
            log.error("Error serving file {}: {}", filePath, e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

   
    public List<IssueListDto> getChildren(String parentKey) {
    	List<IssueListDto> result = new ArrayList<>();
    	List<JiraIssue> children =
    	        issueRepository.findChildrenByParentKey(parentKey);
    	for (JiraIssue issue : children) {
    	    result.add(toListDto(issue));
    	}

    	return result;
    }

    // ─── Mappers ───────────────────────────────────

    private IssueListDto toListDto(JiraIssue i) {
        return IssueListDto.builder()
                .issueKey(i.getIssueKey())
                .summary(i.getSummary())
                .description(i.getDescription())
                .projectKey(i.getProjectKey())
                .projectName(i.getProjectName())
                .issueType(i.getIssueType())
                .priority(i.getPriority())
                .status(i.getStatus())
                .assigneeName(i.getAssignee())
                .reporterName(i.getReporter())
                .build();
    }

    private IssueDetailDto toDetailDto(JiraIssue i,
                                        List<AttachmentDto> attachments,
                                        List<CommentDto> comments) {
        return IssueDetailDto.builder()
                .issueKey(i.getIssueKey())
                .summary(i.getSummary())
                .description(i.getDescription())
                .projectKey(i.getProjectKey())
                .projectName(i.getProjectName())
                .issueType(i.getIssueType())
                .priority(i.getPriority())
                .status(i.getStatus())
                .statusCategory(i.getStatusCategory())
                .assigneeName(i.getAssignee())
                .reporterName(i.getReporter())
                .severity(i.getSeverity())
                .clientName(i.getClientName())
                .labels(i.getLabels())
                .components(i.getComponents())
                .sprint(i.getSprint())
                .created(i.getCreated())
                .updated(i.getUpdated())
                .resolved(i.getResolved())
                .startDate(i.getStartDate())
                .actualStartDate(i.getActualStartDate())
                .actualEndDate(i.getActualEndDate())
                .parentKey(i.getParentKey())
                .parentSummary(i.getParentSummary())
                .storyPoints(i.getStoryPoints())
                .epicName(i.getEpicName())
                .releaseNotes(i.getReleaseNotes())
                .rootCause(i.getRootCause())
                .zohoDeskTicket(i.getZohoDeskTicket())
                .environment(i.getEnvironment())
                .issueReason(i.getIssueReason())
                .issueIn(i.getIssueIn())
                .ocrId(i.getOcrId())
                .fixVersions(i.getFixVersions())
                .redmineTrackerId(i.getRedmineTrackerId())
                .attachments(attachments)
                .comments(comments)
                .build();
    }

    @Value("${app.base-url:http://localhost:8085}")
    private String serverBaseUrl;

    private AttachmentDto toAttachmentDto(JiraAttachment a) {
        return AttachmentDto.builder()
                .id(a.getId())
                .filename(a.getFilename())
                .uploadedAt(a.getUploadedAt())
                .uploadedBy(a.getUserName())
                .downloadUrl(serverBaseUrl + "/api/attachments/" + a.getId() + "/download")
                .build();
    }
  
    private CommentDto toCommentDto(JiraComment c) {
        return CommentDto.builder()
        		.userId(c.getUserId())
                .userName(c.getUserName())
                .commentText(c.getCommentText())
                .commentDate(c.getCommentDate())
                .build();
    }
}