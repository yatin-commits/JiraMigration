package com.ds.app.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "jira_attachment", indexes = {
    @Index(name = "idx_attachment_issue_key", columnList = "issue_key")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JiraAttachment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "issue_key", length = 50, nullable = false)
    private String issueKey;

    @Column(name = "parent_key", length = 50)
    private String parentKey;

    @Column(name = "uploaded_at", length = 50)
    private String uploadedAt;

    @Column(name = "user_id", length = 100)
    private String userId;

    @Column(name = "user_name", length = 255)
    private String userName;
    @Column(name = "issue_type", length = 100)
    private String issueType;

    @Column(name = "filename", length = 500)
    private String filename;

    @Column(name = "jira_url", length = 1000)
    private String jiraUrl;

   
    @Column(name = "ftp_path", length = 1000)
    private String ftpPath;
}