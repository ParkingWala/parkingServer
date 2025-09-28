package com.parkingServer.dto;

import lombok.Data;

@Data
public class AuthTokenResponse {
    private String access_token;
    private String encrypted_access_token;
    private Long issued_at;
    private Long expires_at;
    private String token_type;
}
