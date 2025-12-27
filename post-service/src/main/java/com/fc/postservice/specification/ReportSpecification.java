package com.fc.postservice.specification;

import com.fc.postservice.model.Report;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.jpa.domain.Specification;

import java.time.Instant;

/**
 * Specification utilities for building dynamic report queries.
 */
@Slf4j
public class ReportSpecification {

    /**
     * Creates a Specification to filter reports by creation timestamp.
     *
     * @param fromInclusive start timestamp (inclusive), or null
     * @param toExclusive   end timestamp (exclusive), or null
     * @return Specification for filtering reports between the timestamps
     */
    public static Specification<Report> createdBetween(Instant fromInclusive, Instant toExclusive) {

        // ---- Create final local copies for use inside lambda ----
        Instant finalFrom = fromInclusive;
        Instant finalTo = toExclusive;

        // ---- Fix invalid range (swap if from > to) ----
        if (finalFrom != null && finalTo != null && finalFrom.isAfter(finalTo)) {
            log.warn("createdBetween: Invalid range (from > to). Swapping timestamps.");

            Instant temp = finalFrom;
            finalFrom = finalTo;
            finalTo = temp;
        }

        Instant finalFromCopy = finalFrom;
        Instant finalToCopy = finalTo;

        return (root, query, cb) -> {

            if (finalFromCopy == null && finalToCopy == null) {
                log.debug("createdBetween: No valid date filter applied.");
                return null;
            }

            if (finalFromCopy == null) {
                log.debug("createdBetween: createdAt < {}", finalToCopy);
                return cb.lessThan(root.get("createdAt"), finalToCopy);
            }

            if (finalToCopy == null) {
                log.debug("createdBetween: createdAt >= {}", finalFromCopy);
                return cb.greaterThanOrEqualTo(root.get("createdAt"), finalFromCopy);
            }

            log.debug("createdBetween: createdAt BETWEEN {} and {}", finalFromCopy, finalToCopy);
            return cb.between(root.get("createdAt"), finalFromCopy, finalToCopy);
        };
    }
}