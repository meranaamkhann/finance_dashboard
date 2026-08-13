package com.finance.dashboard.repository;

import com.finance.dashboard.model.Workspace;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WorkspaceRepository extends JpaRepository<Workspace, Long> {

    Optional<Workspace> findByOwnerId(Long ownerId);

    @Query("""
        SELECT w
        FROM Workspace w
        JOIN w.members m
        WHERE m.user.id = :userId
    """)
    List<Workspace> findAllByMemberUserId(@Param("userId") Long userId);
}