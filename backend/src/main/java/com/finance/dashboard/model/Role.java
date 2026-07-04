package com.finance.dashboard.model;

/**
 * System roles with increasing privilege levels.
 *
 * VIEWER  → read-only dashboard access
 * ANALYST → read + full analytics access
 * ADMIN   → full system management
 */
public enum Role {
    VIEWER, ANALYST, ADMIN
}
