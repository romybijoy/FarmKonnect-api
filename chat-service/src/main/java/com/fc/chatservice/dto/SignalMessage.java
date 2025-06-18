package com.fc.chatservice.dto;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class SignalMessage {

    private String type; // offer / answer / candidate
    private String from;
    private String to;
    private Object sdp;        // optional
    private Object candidate;

}
