package com.ds.app.DTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
 
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommentDto {
    private String userId;        
    private String userName;      
    private String commentText;   
    private String commentDate;   
}
 