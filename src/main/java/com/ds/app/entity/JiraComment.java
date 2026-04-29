package com.ds.app.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "jira_comment", indexes = {
    @Index(name = "idx_comment_issue_key", columnList = "issue_key")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JiraComment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "issue_key", length = 50, nullable = false)
    private String issueKey;

    @Column(name = "comment_date", length = 50)
    private String commentDate;

    @Column(name = "user_id", length = 100)
    private String userId;

    // Resolved name from mapping (userId → name)
    @Column(name = "user_name", length = 255)
    private String userName;

    @Lob
    @Column(name = "comment_text", columnDefinition = "LONGTEXT")
    private String commentText;
}