package com.cinema.service;

import com.cinema.dto.Dtos.BookingDto;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import org.springframework.stereotype.Service;
import java.io.ByteArrayOutputStream;
import java.time.format.DateTimeFormatter;

/** Builds the e-ticket PDF in memory with iText 7 and returns the raw bytes. */
@Service
public class TicketPdfService {
    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("EEE d MMM yyyy, HH:mm");

    public byte[] generate(BookingDto b) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        // try-with-resources closes the Document, which also finishes and closes the PDF.
        try (Document doc = new Document(new PdfDocument(new PdfWriter(out)))) {
            doc.add(new Paragraph("CINEMA E-TICKET").setBold().setFontSize(22));
            doc.add(new Paragraph(b.movieTitle()).setBold().setFontSize(18));
            doc.add(new Paragraph("When: " + b.startTime().format(FMT)));
            doc.add(new Paragraph("Hall: " + b.hallName()));
            doc.add(new Paragraph("Seats: " + String.join(", ", b.seats())));
            doc.add(new Paragraph("Name: " + b.customerName()));
            doc.add(new Paragraph("Total paid: " + b.total()));
            doc.add(new Paragraph("Booking code: " + b.code()).setBold().setFontSize(16));
            doc.add(new Paragraph("Show this code at the entrance."));
        }
        return out.toByteArray();
    }
}
