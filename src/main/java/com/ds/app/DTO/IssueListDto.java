package com.ds.app.DTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
 
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IssueListDto {
    private String issueKey;
    private String summary;
    private String description;
    private String projectKey;
    private String projectName;
    private String issueType;
    private String priority;
    private String status;
    private String assigneeName;
    private String reporterName;
}