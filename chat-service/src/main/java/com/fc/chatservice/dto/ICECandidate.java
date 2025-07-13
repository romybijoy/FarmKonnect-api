package com.fc.chatservice.dto;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class ICECandidate {
    private String candidate;
    private String sdpMid;
    private Integer sdpMLineIndex;
}
