package com.fc.postservice.specification;

import com.fc.postservice.model.Report;
import org.springframework.data.jpa.domain.Specification;

import java.time.Instant;

public class ReportSpecification {

    public static Specification<Report> createdBetween(Instant fromInclusive, Instant toExclusive) {
        return (root, query, cb) -> {
            if (fromInclusive == null && toExclusive == null) return null;
            if (fromInclusive == null) {
                return cb.lessThan(root.get("createdAt"), toExclusive);
            } else if (toExclusive == null) {
                return cb.greaterThanOrEqualTo(root.get("createdAt"), fromInclusive);
            } else {
                return cb.between(root.get("createdAt"), fromInclusive, toExclusive);
            }
        };
    }
}