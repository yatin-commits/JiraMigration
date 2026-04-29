package com.ds.app.repository;

import com.ds.app.entity.JiraIssue;

import java.util.Collection;
import java.util.Set;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface JiraIssueRepository extends JpaRepository<JiraIssue, String> {

@Query("select j.issueKey from JiraIssue j where j.issueKey in :keys")
    Set<String> findExistingIssueKeys(@Param("keys") Collection<String> keys);

}