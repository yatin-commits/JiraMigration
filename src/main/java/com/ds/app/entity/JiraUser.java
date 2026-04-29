package com.ds.app.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "jira_user")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JiraUser {

    @Id
    @Column(name = "user_id", length = 100)
    private String userId;

    @Column(name = "user_name", length = 255, nullable = false)
    private String userName;
}