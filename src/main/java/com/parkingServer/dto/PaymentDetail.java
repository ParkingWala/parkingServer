package com.parkingServer.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class PaymentDetail {

    private String paymentMode;
    private String transactionId;
    private Long timestamp;
    private Long amount;
    private Long payableAmount;
    private Long feeAmount;
    private String state;
    private String errorCode;
    private String detailedErrorCode;

    private Rail rail = new Rail(); // Nested object
    private Instrument instrument = new Instrument(); // Nested object

    // getters and setters

    // Optional inner classes
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class Rail {
        private String type;
        private String utr;
        private String upiTransactionId;
        private String vpa;

        // getters and setters
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class Instrument {
        private String type;
        private String maskedAccountNumber;
        private String accountType;
        private String accountHolderName;

        // getters and setters
    }
}
