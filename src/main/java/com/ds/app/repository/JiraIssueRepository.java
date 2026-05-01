package com.ds.app.repository;

import com.ds.app.entity.JiraIssue;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;

@Repository
public interface JiraIssueRepository extends JpaRepository<JiraIssue, String> {

	@Query("SELECT i.issueKey FROM JiraIssue i WHERE i.issueKey IN :keys")
	Set<String> findExistingIssueKeys(@Param("keys") Set<String> keys);

	JiraIssue findByIssueKey(String issueKey);

	@Query("SELECT DISTINCT i.projectKey, i.projectName FROM JiraIssue i "
			+ "WHERE i.projectKey IS NOT NULL ORDER BY i.projectKey")
	List<Object[]> findDistinctProjects();

	@Query("SELECT DISTINCT i.issueType FROM JiraIssue i " + "WHERE i.issueType IS NOT NULL ORDER BY i.issueType")
	List<String> findDistinctIssueTypes();

	@Query("SELECT i.issueKey FROM JiraIssue i "
			+ "WHERE UPPER(i.issueKey) LIKE CONCAT(:query, '%') ORDER BY i.issueKey")
	List<String> findIssueKeysByQuery(@Param("query") String query, Pageable pageable);

	@Query("SELECT i FROM JiraIssue i WHERE " +
		       "(:projectKey IS NULL OR i.projectKey = :projectKey) AND " +
		       "(:issueType  IS NULL OR i.issueType  = :issueType)  AND " +
		       "(:issueKey   IS NULL OR i.issueKey   = :issueKey)   AND " +
		       "(:search IS NULL OR (" +        
		       "  i.summary     LIKE CONCAT('%', :search, '%') OR " +
		       "  i.description LIKE CONCAT('%', :search, '%') OR " +
		       "  i.issueKey    LIKE CONCAT('%', :search, '%') OR " +
		       "  i.issueType   LIKE CONCAT('%', :search, '%') OR " +
		       "  i.status      LIKE CONCAT('%', :search, '%') OR " +
		       "  i.assignee    LIKE CONCAT('%', :search, '%') OR " +
		       "  i.reporter    LIKE CONCAT('%', :search, '%')" +
		       "))")                             
		Page<JiraIssue> findWithFilters(
		        @Param("projectKey") String projectKey,
		        @Param("issueType")  String issueType,
		        @Param("issueKey")   String issueKey,
		        @Param("search")     String search,
		        Pageable pageable
		);

	@Query("SELECT i FROM JiraIssue i " + "WHERE i.parentKey = :parentKey " + "ORDER BY i.issueKey ASC")
	List<JiraIssue> findChildrenByParentKey(@Param("parentKey") String parentKey);
}