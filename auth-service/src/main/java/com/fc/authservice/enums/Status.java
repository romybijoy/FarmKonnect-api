package com.fc.authservice.enums;

/**
 * Enumeration representing the status of a user account.
 * ACTIVE  - The user account is active and fully functional.
 * BLOCKED - The user account is restricted from accessing certain or all features.
 * This enum is typically used in:
 * - User account management
 * - Authentication and authorization flows
 * - Admin dashboard and moderation tools
 * Additional statuses can be added as the application grows.
 *
 * @since 2025
 * @author
 *   Romy Rose Jimmy
 */
public enum Status {
    ACTIVE,
    BLOCKED
}
