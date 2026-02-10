package com.fc.postservice.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.util.List;

/**
 * DTO for update post
 *
 * @author Romy Rose Jimmy
 * @since 20/12/2025
 */

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class UpdatePostRequest {

    private String content;
    private List<String> postImages;

}
