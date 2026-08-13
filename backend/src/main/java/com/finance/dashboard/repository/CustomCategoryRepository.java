package com.finance.dashboard.repository;

import com.finance.dashboard.model.CustomCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.Optional;

import java.util.List;

@Repository
public interface CustomCategoryRepository extends JpaRepository<CustomCategory, Long> {

    Optional<CustomCategory> findByNameIgnoreCaseAndSystemTrue(String name);

    @Query("""
        SELECT c FROM CustomCategory c
        WHERE c.workspaceId = :wsId OR c.system = true
        ORDER BY c.name
    """)
    List<CustomCategory> findByWorkspaceIdOrSystem(@Param("wsId") Long workspaceId);

    boolean existsByNameAndWorkspaceId(String name, Long workspaceId);

    boolean existsByNameAndSystemTrue(String name);
}