package com.fc.authservice.config;

public class AppConstants {
    public static final String PAGE_NUMBER = "0";
    public static final String PAGE_SIZE = "2";

    public static final String SORT_USERS_BY = "id";
    public static final String SORT_DIR = "desc";
    public static final String STATUS = "true";

    public static final String USER_NOT_FOUND = "User not found";
    public static final String USER_NOT_FOUND_EMAIL = "User not found with this email: ";

    private AppConstants() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }
}
