package com.ds.app.repository;

import com.ds.app.entity.JiraComment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
public interface JiraCommentRepository extends JpaRepository<JiraComment, Long> {
    List<JiraComment> findByIssueKeyOrderByCommentDateDesc(String issueKey);
    
    List<JiraComment> findByIssueKeyIn(Collection<String> issueKeys);
}