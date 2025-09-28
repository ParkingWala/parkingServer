package com.parkingServer.dto;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Data
public class OrderStatusResponse {
    private String orderId;
    private String state;
    private Long amount;
    private Long expireAt;
//    private List<Map<String,Object>> paymentDetails;
    private Map<String,Object> errorContext;
    private List<PaymentDetail> paymentDetails = new ArrayList<>(); // <- initialize

}
