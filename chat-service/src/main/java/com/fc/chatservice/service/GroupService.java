package com.fc.chatservice.service;

import com.fc.chatservice.dto.CreateGroupRequest;
import com.fc.chatservice.dto.GroupDto;
import com.fc.chatservice.exception.GroupNotFoundException;
import com.fc.chatservice.model.ChatGroup;
import com.fc.chatservice.model.GroupMember;
import com.fc.chatservice.repository.ChatGroupRepository;
import com.fc.chatservice.repository.GroupMemberRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class GroupService {

    @Autowired
    private ChatGroupRepository chatGroupRepository;

    @Autowired
    private GroupMemberRepository groupMemberRepository;

    public ChatGroup createGroup(CreateGroupRequest request) {
        // 1. Create group entity
        ChatGroup group = ChatGroup.builder()
                .name(request.getName())
                .createdBy(request.getCreatorId())
                .createdAt(LocalDateTime.now())
                .build();

        // 2. Save group first to get an ID
        ChatGroup savedGroup = chatGroupRepository.save(group);

        // 3. Map and attach members
        if (request.getMembers() != null) {
            List<GroupMember> members = request.getMembers().stream()
                    .map(memberReq -> GroupMember.builder()
                            .group(savedGroup)
                            .userId(memberReq.getUserId())
                            .isAdmin(memberReq.isAdmin()) // Lombok boolean naming
                            .build())
                    .collect(Collectors.toList());

            groupMemberRepository.saveAll(members);

            // 4. (Optional) Add members to group object for return
            savedGroup.setMembers(members);
        }

        return savedGroup;
    }

    public void addMember(UUID groupId, UUID userId) {
        ChatGroup group = chatGroupRepository.findById(groupId)
                .orElseThrow(() -> new RuntimeException("Group not found"));

        GroupMember member = GroupMember.builder()
                .group(group)
                .userId(userId)
                .isAdmin(false)
                .joinedAt(LocalDateTime.now())
                .build();

        groupMemberRepository.save(member);
    }

    public List<UUID> getMemberIds(UUID groupId) {
        List<GroupMember> members = groupMemberRepository.findByGroupId(groupId);
        return members.stream().map(GroupMember::getUserId).toList();
    }

    public List<ChatGroup> getAllGroups() {
        return chatGroupRepository.findAll();
    }

    public List<ChatGroup> getGroupsByUserId(UUID userId) {
        return chatGroupRepository.findAllByUserId(userId);
    }

    public GroupDto getGroupById(UUID id) {
        ChatGroup g = chatGroupRepository.findByIdWithMembers(id).orElseThrow(() -> new GroupNotFoundException(id));

        return GroupDto.builder()
                .id(g.getId())
                .name(g.getName())
                .createdBy(g.getCreatedBy())
                .createdAt(g.getCreatedAt())
                .memberIds(g.getMembers().stream().map(GroupMember::getUserId).collect(Collectors.toList()))
                .memberCount(g.getMembers().size())
                .build();
    }
}
