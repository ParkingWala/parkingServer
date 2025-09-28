package com.parkingServer.controller;

import com.parkingServer.dto.BookingRequest;
import com.parkingServer.dto.BookingResponse;
import com.parkingServer.entity.Booking;
import com.parkingServer.service.BookingService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/slots")
public class BookingController {

    private final BookingService bookingService;

    public BookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    @PostMapping("/book")
    public ResponseEntity<BookingResponse> bookSlot(@RequestBody BookingRequest req) {
        Booking b = bookingService.createBooking(req);
        BookingResponse res = new BookingResponse();
        res.setBookingId(b.getBookingId());
        res.setStatus(b.getStatus());
        return ResponseEntity.ok(res);
    }
}
