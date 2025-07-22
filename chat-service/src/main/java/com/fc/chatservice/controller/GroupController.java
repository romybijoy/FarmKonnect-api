package com.fc.chatservice.controller;

import com.fc.chatservice.dto.AddMemberRequest;
import com.fc.chatservice.dto.CreateGroupRequest;
import com.fc.chatservice.model.ChatGroup;
import com.fc.chatservice.service.GroupService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/groups")
public class GroupController {

    @Autowired
    private GroupService groupService;

    @PostMapping
    public ResponseEntity<ChatGroup> createGroup(@RequestBody CreateGroupRequest request) {
        return ResponseEntity.ok(groupService.createGroup(request));
    }

    @PostMapping("/{groupId}/members")
    public ResponseEntity<String> addMember(
            @PathVariable UUID groupId,
            @RequestBody AddMemberRequest request) {
        groupService.addMember(groupId, request.getUserId());
        return ResponseEntity.ok("Member added to group");
    }

    @GetMapping("/{groupId}/members")
    public ResponseEntity<List<UUID>> getGroupMembers(@PathVariable UUID groupId) {
        return ResponseEntity.ok(groupService.getMemberIds(groupId));
    }

    @GetMapping
    public ResponseEntity<List<ChatGroup>> getAllGroups() {
        return ResponseEntity.ok(groupService.getAllGroups());
    }
}
