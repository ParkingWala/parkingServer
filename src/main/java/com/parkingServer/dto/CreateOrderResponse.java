package com.parkingServer.dto;

import lombok.Data;

@Data
public class CreateOrderResponse {
    private String orderId;
    private String state;
    private Long expireAt;
    private String token;
}
