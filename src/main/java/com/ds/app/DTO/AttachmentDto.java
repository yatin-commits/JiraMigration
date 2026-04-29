package com.ds.app.DTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
 
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AttachmentDto {
    private Long id;
    private String filename;
    private String uploadedAt;
    private String uploadedBy;   // userName
    private String downloadUrl;  // /api/attachments/{id}/download
}