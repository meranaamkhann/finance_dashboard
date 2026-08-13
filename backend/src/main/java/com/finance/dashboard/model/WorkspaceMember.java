package com.finance.dashboard.model;

import com.finance.dashboard.model.enums.WorkspaceMemberRole;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(
    name = "workspace_members",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uq_workspace_user",
            columnNames = {
                "workspace_id",
                "user_id"
            }
        )
    },
    indexes = {
        @Index(
            name = "idx_wm_workspace",
            columnList = "workspace_id"
        ),
        @Index(
            name = "idx_wm_user",
            columnList = "user_id"
        )
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WorkspaceMember {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
        name = "workspace_id",
        nullable = false
    )
    private Workspace workspace;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
        name = "user_id",
        nullable = false
    )
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(
        nullable = false,
        length = 20
    )
    @Builder.Default
    private WorkspaceMemberRole role =
            WorkspaceMemberRole.VIEWER;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "invited_by")
    private User invitedBy;

    @Column(nullable = false)
    @Builder.Default
    private LocalDateTime joinedAt =
            LocalDateTime.now();
}