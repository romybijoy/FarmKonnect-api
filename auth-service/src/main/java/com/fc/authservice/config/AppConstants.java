package com.fc.authservice.config;

/**
 * Application-wide constant values used in the Auth Service.
 * This class centralizes repeated constants such as pagination defaults,
 * sorting configuration, and common validation or exception messages.
 * Being a utility class, it cannot be instantiated.
 *
 * @author Romy Rose Jimmy
 * @since 2025
 */
public class AppConstants {
    // Pagination defaults
    public static final String PAGE_NUMBER = "0";
    public static final String PAGE_SIZE = "2";

    // Sorting defaults
    public static final String SORT_USERS_BY = "id";
    public static final String SORT_DIR = "desc";

    // Common boolean status
    public static final String STATUS = "true";

    // User-related messages
    public static final String USER_NOT_FOUND = "User not found";
    public static final String USER_NOT_FOUND_EMAIL = "User not found with this email: ";

    /**
     * Private constructor to prevent instantiation.
     * Throws exception if attempted to ensure utility class integrity.
     */
    private AppConstants() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }
}
