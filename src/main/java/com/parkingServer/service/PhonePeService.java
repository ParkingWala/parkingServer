package com.parkingServer.service;

import com.parkingServer.dto.AuthTokenResponse;
import com.parkingServer.dto.CreateOrderRequest;
import com.parkingServer.dto.CreateOrderResponse;
import com.parkingServer.dto.OrderStatusResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.util.Collections;

@Service
public class PhonePeService {

    private static final Logger log = LoggerFactory.getLogger(PhonePeService.class);

    private final RestTemplate restTemplate = new RestTemplate();

    private String cachedToken = null;
    private Long tokenExpiry = null;

    private final String tokenUrl;
    private final String orderUrl;
    private final String statusUrl;
    private final String clientId;
    private final String clientSecret;
    private final String clientVersion;

    public PhonePeService(Environment env,
                          @Value("${phonepe.client-id:}") String clientId,
                          @Value("${phonepe.client-secret:}") String clientSecret,
                          @Value("${phonepe.client-version:1}") String clientVersion) {

        // Allow environment variables to override properties
        this.tokenUrl = env.getProperty("PHONEPE_TOKEN_URL",
                env.getProperty("phonepe.token-url", "https://api-preprod.phonepe.com/apis/pg-sandbox/v1/oauth/token"));

        this.orderUrl = env.getProperty("PHONEPE_ORDER_URL",
                env.getProperty("phonepe.order-url", "https://api-preprod.phonepe.com/apis/pg-sandbox/checkout/v2/sdk/order"));

        this.statusUrl = env.getProperty("PHONEPE_STATUS_URL",
                env.getProperty("phonepe.status-url", "https://api-preprod.phonepe.com/apis/pg-sandbox/checkout/v2/order"));

        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.clientVersion = clientVersion;

        log.info("PhonePeService initialized with clientId: {}", clientId);
    }

    /**
     * Generate OAuth token from PhonePe
     */
    public AuthTokenResponse generateAuthToken() {
        if (clientId.isEmpty() || clientSecret.isEmpty()) {
            log.warn("PhonePe clientId or clientSecret is missing, cannot generate token");
            return null;
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        String body = "client_id=" + clientId +
                "&client_version=" + clientVersion +
                "&client_secret=" + clientSecret +
                "&grant_type=client_credentials";

        try {
            ResponseEntity<AuthTokenResponse> response = restTemplate.exchange(
                    URI.create(tokenUrl),
                    HttpMethod.POST,
                    new HttpEntity<>(body, headers),
                    AuthTokenResponse.class
            );

            AuthTokenResponse tokenResponse = response.getBody();
            if (tokenResponse != null) {
                cachedToken = tokenResponse.getAccess_token();
                tokenExpiry = tokenResponse.getExpires_at();
                log.info("Generated PhonePe token, expires at {}", tokenExpiry);
            } else {
                log.warn("PhonePe token response was null");
            }
            return tokenResponse;

        } catch (Exception e) {
            log.error("Failed to generate PhonePe token: {}", e.getMessage());
            return null;
        }
    }

    /**
     * Create a new order
     */
    public CreateOrderResponse createOrder(CreateOrderRequest req) {
        ensureTokenValid();
        if (cachedToken == null) {
            log.warn("Skipping createOrder because token is not available");
            return new CreateOrderResponse();
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "O-Bearer " + cachedToken);

        // Default to PG_CHECKOUT if flow not provided
        if (req.getPaymentFlow() == null || req.getPaymentFlow().getType() == null) {
            req.setPaymentFlow(new CreateOrderRequest.PaymentFlow("PG_CHECKOUT"));
        }

        try {
            ResponseEntity<CreateOrderResponse> response = restTemplate.exchange(
                    URI.create(orderUrl),
                    HttpMethod.POST,
                    new HttpEntity<>(req, headers),
                    CreateOrderResponse.class
            );
            return response.getBody() != null ? response.getBody() : new CreateOrderResponse();

        } catch (HttpServerErrorException | HttpClientErrorException e) {
            log.warn("PhonePe server error during createOrder: {} {}", e.getStatusCode(), e.getResponseBodyAsString());
            return new CreateOrderResponse();

        } catch (Exception e) {
            log.error("Unexpected error while creating order: {}", e.getMessage());
            return new CreateOrderResponse();
        }
    }

    /**
     * Check order status
     */
    public OrderStatusResponse checkStatus(String merchantOrderId) {
        ensureTokenValid();
        if (cachedToken == null) {
            log.warn("Skipping checkStatus because token is not available");
            return defaultUnknownStatus(merchantOrderId);
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "O-Bearer " + cachedToken);

        String url = statusUrl + "/" + merchantOrderId + "/status?details=false&errorContext=true";

        try {
            ResponseEntity<OrderStatusResponse> response = restTemplate.exchange(
                    URI.create(url),
                    HttpMethod.GET,
                    new HttpEntity<>(headers),
                    OrderStatusResponse.class
            );
            return sanitizeStatus(response.getBody(), merchantOrderId);

        } catch (HttpServerErrorException | HttpClientErrorException e) {
            log.warn("PhonePe server error for order {}: {} {}", merchantOrderId, e.getStatusCode(), e.getResponseBodyAsString());
            return defaultUnknownStatus(merchantOrderId);

        } catch (Exception e) {
            log.error("Unexpected error while checking order {}: {}", merchantOrderId, e.getMessage());
            return defaultUnknownStatus(merchantOrderId);
        }
    }

    /**
     * Ensure we always return a usable status object
     */
    private OrderStatusResponse sanitizeStatus(OrderStatusResponse status, String merchantOrderId) {
        if (status == null) return defaultUnknownStatus(merchantOrderId);
        if (status.getOrderId() == null) status.setOrderId(merchantOrderId);
        if (status.getState() == null) status.setState("UNKNOWN");
        if (status.getPaymentDetails() == null) status.setPaymentDetails(Collections.emptyList());
        return status;
    }

    private OrderStatusResponse defaultUnknownStatus(String merchantOrderId) {
        OrderStatusResponse unknown = new OrderStatusResponse();
        unknown.setOrderId(merchantOrderId);
        unknown.setState("UNKNOWN");
        unknown.setPaymentDetails(Collections.emptyList());
        return unknown;
    }

    /**
     * Refresh token if missing or expired
     */
    private void ensureTokenValid() {
        if (cachedToken == null || (tokenExpiry != null && System.currentTimeMillis() / 1000 >= tokenExpiry)) {
            generateAuthToken();
        }
    }
}
