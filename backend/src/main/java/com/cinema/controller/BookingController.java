package com.cinema.controller;

import com.cinema.dto.Dtos.*;
import com.cinema.service.BookingService;
import com.cinema.service.TicketPdfService;
import jakarta.validation.Valid;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

/** /api/bookings - checkout and e-ticket download. */
@RestController
@RequestMapping("/api/bookings")
public class BookingController {
    private final BookingService bookingService;
    private final TicketPdfService pdfService;

    public BookingController(BookingService bookingService, TicketPdfService pdfService) {
        this.bookingService = bookingService; this.pdfService = pdfService;
    }

    @PostMapping
    public BookingDto checkout(@Valid @RequestBody CheckoutRequest req) { return bookingService.checkout(req); }

    /** Returns the PDF; the browser downloads it because of the attachment header. */
    @GetMapping("/{code}/ticket")
    public ResponseEntity<byte[]> ticket(@PathVariable String code) {
        byte[] pdf = pdfService.generate(bookingService.getByCode(code));
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=ticket-" + code + ".pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }
}
