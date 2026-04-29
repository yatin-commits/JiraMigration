package com.ds.app.DTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
 
import java.util.List;
 
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IssueDetailDto {
    private String issueKey;
    private String summary;
    private String description;
    private String projectKey;
    private String projectName;
    private String issueType;
    private String priority;
    private String status;
    private String statusCategory;
    private String assigneeName;
    private String reporterName;
    private String severity;
    private String clientName;
    private String labels;
    private String components;
    private String sprint;
    private String created;
    private String updated;
    private String resolved;
    private String startDate;
    private String actualStartDate;
    private String actualEndDate;
    private String parentKey;
    private String parentSummary;
    private String storyPoints;
    private String epicName;
    private String releaseNotes;
    private String rootCause;
    private String redmineTrackerId;
    private String zohoDeskTicket;
    private String environment;
    private String issueReason;   
    private String issueIn;
    private String ocrId;
    private String fixVersions;
 
    // Related data
    private List<AttachmentDto> attachments;
    private List<CommentDto>    comments;
}
