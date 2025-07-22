package com.fc.chatservice.dto;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Data
public class CreateGroupRequest {
    private String name;
    private UUID creatorId;
    private List<AddMemberRequest> members = new ArrayList<>();

}