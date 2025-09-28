package com.parkingServer.dto;

import lombok.Data;

@Data
public class BookingRequest {
    private String slotId;
    private String userId;
    private Long amount; // in paisa
}
