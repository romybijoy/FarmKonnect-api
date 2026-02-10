package com.fc.chatservice.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

/**
 * Exception Handling for GroupNotFoundException
 *
 * @author Romyb
 * @since 12/12/2025
 */
public class GroupNotFoundException extends ResponseStatusException {
    public GroupNotFoundException(UUID id) {
        super(HttpStatus.NOT_FOUND, "Group not found: " + id);
    }
}
