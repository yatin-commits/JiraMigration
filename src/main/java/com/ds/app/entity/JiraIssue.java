package com.ds.app.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "jira_issue")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JiraIssue {

   
    @Id
    @Column(name = "issue_key", length = 120, nullable = false)
    private String issueKey;

    
    @Column(name = "issue_id", length = 50)
    private String issueId;

    @Column(name = "issue_type", length = 100)
    private String issueType;

    @Column(name = "status", length = 100)
    private String status;

    @Column(name = "status_category", length = 100)
    private String statusCategory;

    @Column(name = "project_key", length = 50)
    private String projectKey;

    @Column(name = "project_name", length = 255)
    private String projectName;

  
    @Column(name = "summary", length = 1000)
    private String summary;

    @Lob
    @Column(name = "description", columnDefinition = "LONGTEXT")
    private String description;

    @Column(name = "priority", length = 50)
    private String priority;

    
    @Column(name = "reporter", length = 255)
    private String reporter;

    @Column(name = "reporter_id", length = 100)
    private String reporterId;

    @Column(name = "assignee", length = 255)
    private String assignee;

    @Column(name = "assignee_id", length = 100)
    private String assigneeId;

    
    @Column(name = "created", length = 50)
    private String created;

    @Column(name = "updated", length = 50)
    private String updated;

    @Column(name = "resolved", length = 50)
    private String resolved;

    
    @Lob
    @Column(name = "labels", columnDefinition = "TEXT")
    private String labels;

    @Column(name = "parent_key", length = 50)
    private String parentKey;

    @Column(name = "parent_summary", length = 1000)
    private String parentSummary;

   
    @Column(name = "severity", length = 100)
    private String severity;

    @Column(name = "client_name", length = 255)
    private String clientName;

    @Column(name = "start_date", length = 50)
    private String startDate;

    @Column(name = "actual_start_date", length = 50)
    private String actualStartDate;

    @Column(name = "actual_end_date", length = 50)
    private String actualEndDate;

    @Column(name = "redmine_tracker_id", length = 100)
    private String redmineTrackerId;

    @Lob
    @Column(name = "release_notes", columnDefinition = "LONGTEXT")
    private String releaseNotes;

    @Lob
    @Column(name = "issue_reason", columnDefinition = "TEXT")
    private String issueReason;

    @Lob
    @Column(name = "issue_in", columnDefinition = "TEXT")
    private String issueIn;

    @Lob
    @Column(name = "root_cause", columnDefinition = "LONGTEXT")
    private String rootCause;

    @Column(name = "epic_name", length = 255)
    private String epicName;

    @Lob
    @Column(name = "sprint", columnDefinition = "TEXT")
    private String sprint;

    @Column(name = "story_points", length = 50)
    private String storyPoints;

    @Lob
    @Column(name = "components", columnDefinition = "TEXT")
    private String components;

    @Lob
    @Column(name = "fix_versions", columnDefinition = "TEXT")
    private String fixVersions;

    @Lob
    @Column(name = "environment", columnDefinition = "TEXT")
    private String environment;

   
    @Column(name = "ocr_id", length = 100)
    private String ocrId;

    @Lob
    @Column(name = "zoho_desk_ticket", columnDefinition = "LONGTEXT")
    private String zohoDeskTicket;
}