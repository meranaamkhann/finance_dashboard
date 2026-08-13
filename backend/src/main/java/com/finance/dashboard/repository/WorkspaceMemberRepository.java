package com.finance.dashboard.repository;

import com.finance.dashboard.model.WorkspaceMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WorkspaceMemberRepository
        extends JpaRepository<WorkspaceMember, Long> {

    Optional<WorkspaceMember> findByWorkspaceIdAndUserId(
            Long workspaceId,
            Long userId
    );

    List<WorkspaceMember> findAllByWorkspaceId(
            Long workspaceId
    );

    boolean existsByWorkspaceIdAndUserId(
            Long workspaceId,
            Long userId
    );

    int countByWorkspaceId(
            Long workspaceId
    );

    @Query("""
        SELECT wm
        FROM WorkspaceMember wm
        WHERE wm.user.id = :userId
    """)
    List<WorkspaceMember> findAllByUserId(
            @Param("userId") Long userId
    );

    void deleteByWorkspaceIdAndUserId(
            Long workspaceId,
            Long userId
    );
}