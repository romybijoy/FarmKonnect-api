package com.fc.authservice.dto;

import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
public class UserIdsRequest {

    private List<UUID> ids;

}
