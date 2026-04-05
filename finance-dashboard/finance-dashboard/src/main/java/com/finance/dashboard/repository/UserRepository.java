package com.finance.dashboard.repository;

import com.finance.dashboard.model.Role;
import com.finance.dashboard.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);

    Page<User> findAllByRole(Role role, Pageable pageable);
    Page<User> findAllByActiveTrue(Pageable pageable);

    @Modifying
    @Query("UPDATE User u SET u.failedLoginAttempts = 0, u.lastLoginAt = CURRENT_TIMESTAMP WHERE u.id = :id")
    void resetFailedAttemptsAndUpdateLogin(@Param("id") Long id);

    @Modifying
    @Query("UPDATE User u SET u.failedLoginAttempts = u.failedLoginAttempts + 1 WHERE u.username = :username")
    void incrementFailedAttempts(@Param("username") String username);
}
