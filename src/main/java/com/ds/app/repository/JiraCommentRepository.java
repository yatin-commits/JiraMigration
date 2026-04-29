package com.ds.app.repository;

 
import com.ds.app.entity.JiraComment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
 
import java.util.List;
import java.util.Set;
 
@Repository
public interface JiraCommentRepository extends JpaRepository<JiraComment, Long> {
 
    // Already exists — used by CsvImportService
    List<JiraComment> findByIssueKeyIn(Set<String> issueKeys);
 
    // ← UPDATED: chronological order by commentDate
    @Query("SELECT c FROM JiraComment c " +
           "WHERE c.issueKey = :issueKey " +
           "ORDER BY c.commentDate ASC")
    List<JiraComment> findByIssueKeyOrderByDate(@Param("issueKey") String issueKey);
}