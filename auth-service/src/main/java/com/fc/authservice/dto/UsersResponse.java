package com.fc.authservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * A standardized response DTO for returning paginated lists of users.
 * This DTO is typically used in dashboard listings, admin panels, and
 * any endpoint that fetches multiple users with pagination support.
 * It contains:
 * - Status code and message describing the result
 * - A list of user data (content)
 * - Pagination metadata: page number, size, total items, total pages, and last-page flag
 * This structure helps the frontend easily handle paginated UI rendering.
 *
 * @author Romy Rose Jimmy
 * @since 2025
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UsersResponse {

    /** Operation status code (200, 400, etc.) */
    private int statusCode;

    /** Human-readable message describing the response result */
    private String message;

    /** List of user data returned for the current page */
    private List<UserDTO> content;

    /** Current page number */
    private Integer pageNumber;

    /** Number of records per page */
    private Integer pageSize;

    /** Total number of matching records across all pages */
    private Long totalElements;

    /** Total number of available pages */
    private Integer totalPages;

    /** Whether the current page is the last page */
    private boolean lastPage;

}