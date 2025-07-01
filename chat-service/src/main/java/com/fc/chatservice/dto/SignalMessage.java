package com.fc.chatservice.dto;

import lombok.Getter;
import lombok.Setter;


@Setter
@Getter
public class SignalMessage {


        private String type; // "offer", "answer", "ice"
        private String callerId;
        private String receiverId;

        // For SDP (offer/answer)
        private String sdp;

        private String targetId;
        // For ICE candidate
        private String candidate;
        private String sdpMid;
        private Integer sdpMLineIndex;



}
