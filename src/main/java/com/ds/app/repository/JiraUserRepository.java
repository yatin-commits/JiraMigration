package com.ds.app.repository;

import com.ds.app.entity.JiraUser;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface JiraUserRepository extends JpaRepository<JiraUser, String> {
}