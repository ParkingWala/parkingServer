package com.parkingServer.controller;

import com.parkingServer.dto.AuthTokenResponse;
import com.parkingServer.dto.CreateOrderRequest;
import com.parkingServer.dto.CreateOrderResponse;
import com.parkingServer.dto.OrderStatusResponse;
import com.parkingServer.entity.Booking;
import com.parkingServer.repository.BookingRepository;
import com.parkingServer.service.PhonePeService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/payment")
public class PaymentController {

    private final PhonePeService phonePeService;
    private final BookingRepository bookingRepository;

    public PaymentController(PhonePeService phonePeService, BookingRepository bookingRepository) {
        this.phonePeService = phonePeService;
        this.bookingRepository = bookingRepository;
    }

    @PostMapping("/token")
    public ResponseEntity<AuthTokenResponse> getToken() {
        return ResponseEntity.ok(phonePeService.generateAuthToken());
    }

    @PostMapping("/order")
    public ResponseEntity<CreateOrderResponse> createOrder(@RequestBody CreateOrderRequest req) {
        // update booking status to PAYMENT_PENDING if booking exists
        if (req.getMerchantOrderId() != null) {
            Optional<Booking> ob = bookingRepository.findByBookingId(req.getMerchantOrderId());
            ob.ifPresent(b -> {
                b.setStatus("PAYMENT_PENDING");
                bookingRepository.save(b);
            });
        }
        CreateOrderResponse resp = phonePeService.createOrder(req);
        return ResponseEntity.ok(resp);
    }

    @GetMapping("/status/{merchantOrderId}")
    public ResponseEntity<OrderStatusResponse> status(@PathVariable String merchantOrderId) {
        OrderStatusResponse resp = phonePeService.checkStatus(merchantOrderId);
        // if completed, update booking
        if (resp != null && "COMPLETED".equalsIgnoreCase(resp.getState())) {
            bookingRepository.findByBookingId(merchantOrderId).ifPresent(b -> {
                b.setStatus("COMPLETED");
                bookingRepository.save(b);
            });
        }
        return ResponseEntity.ok(resp);
    }
}
