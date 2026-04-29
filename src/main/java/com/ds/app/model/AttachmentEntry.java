package com.ds.app.model;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AttachmentEntry {
    private String issueKey;
    private String parentKey;   // Epic/Parent key — empty if no parent
    private String issueType;   // e.g. "Epic", "Story", "Task", "Bug" etc.
    private String uploadedAt;
    private String userId;
    private String filename;
    private String url;
}