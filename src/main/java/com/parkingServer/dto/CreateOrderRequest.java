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

        public PaymentFlow() {
        }

        public PaymentFlow(String type) {
            this.type = type;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }
    }
}
