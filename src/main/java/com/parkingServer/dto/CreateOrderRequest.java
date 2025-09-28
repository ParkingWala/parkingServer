package com.parkingServer.dto;

import lombok.Data;
import java.util.Map;

@Data
public class CreateOrderRequest {
    private String merchantOrderId;
    private Long amount;
    private Long expireAfter;
    private Map<String,String> metaInfo;
    private PaymentFlow paymentFlow;

    @Data
    public static class PaymentFlow {
        private String type;
    }
}
