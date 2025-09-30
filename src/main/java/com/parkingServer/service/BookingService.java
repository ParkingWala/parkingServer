package com.parkingServer.service;

import com.parkingServer.dto.BookingRequest;
import com.parkingServer.entity.Booking;
import com.parkingServer.repository.BookingRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public class BookingService {

    private final BookingRepository bookingRepository;

    public BookingService(BookingRepository bookingRepository) {
        this.bookingRepository = bookingRepository;
    }

    public Booking createBooking(BookingRequest req) {
        Booking b = new Booking();
        b.setBookingId("BK-" + UUID.randomUUID().toString().replaceAll("-","" ).substring(0,12));
        b.setSlotId(req.getSlotId());
        b.setUserId(req.getUserId());
        b.setAmount(req.getAmount());
        b.setStatus("PAYMENT_PENDING");
        b.setCreatedAt(Instant.now());
        return bookingRepository.save(b);
    }
    public List<Booking> getAllBookings() {
        return bookingRepository.findAll();
    }

}
