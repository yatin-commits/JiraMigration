package com.ds.app.repository;

import com.ds.app.entity.JiraAttachment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
public interface JiraAttachmentRepository extends JpaRepository<JiraAttachment, Long> {
    List<JiraAttachment> findByIssueKey(String issueKey);
    
    List<JiraAttachment> findByIssueKeyIn(Collection<String> issueKeys);
}