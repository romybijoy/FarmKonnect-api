package com.fc.postservice.dto.admin;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Generic paginated response wrapper used by the Admin Post APIs.
 * Contains:
 *  - Status and message for API response clarity
 *  - List of posts in the current page
 *  - Pagination metadata (page number, size, total count, etc.)
 * This structure is returned by:
 *  - AdminPostService
 *  - AdminPostController
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PostResponse {

    /** HTTP-like custom status code for admin responses (e.g., 200, 404, 500) */
    private int statusCode;

    /** Human-readable response message */
    private String message;

    /** List of posts returned in the page */
    private List<AdminPostDto> content;

    /** Current page index (0-based) */
    private Integer pageNumber;

    /** Page size requested */
    private Integer pageSize;

    /** Total number of elements across all pages */
    private Long totalElements;

    /** Total number of pages available */
    private Integer totalPages;

    /** Indicates if this is the last page in pagination */
    private boolean lastPage;

}