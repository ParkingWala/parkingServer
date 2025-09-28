package com.parkingServer.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.Instant;

@Entity
@Data
public class Booking {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String bookingId; // merchant order id
    private String slotId;
    private String userId;
    private Long amount; // in paisa
    private String status; // BOOKED, PAYMENT_PENDING, COMPLETED, FAILED
    private Instant createdAt;
}
