package com.finance.dashboard.service;

import com.finance.dashboard.exception.ResourceNotFoundException;
import com.finance.dashboard.model.Payment;
import com.finance.dashboard.repository.PaymentRepository;
import com.finance.dashboard.util.SecurityUtils;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.io.ByteArrayOutputStream;
import java.time.format.DateTimeFormatter;

@Slf4j
@Service
@RequiredArgsConstructor
public class InvoiceService {

    private final PaymentRepository paymentRepository;
    private final SecurityUtils securityUtils;

    private static final DeviceRgb BRAND_BLUE = new DeviceRgb(37, 99, 235);
    private static final DeviceRgb LIGHT_GRAY = new DeviceRgb(248, 250, 252);
    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd MMM yyyy");

    @Transactional(readOnly = true)
    public byte[] generateInvoicePdf(Long paymentId) {
        Long userId = securityUtils.getCurrentUserId();
        Payment payment = paymentRepository.findById(paymentId)
                .filter(p -> p.getUser().getId().equals(userId))
                .orElseThrow(() -> new ResourceNotFoundException("Payment", paymentId));

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            PdfWriter writer = new PdfWriter(baos);
            PdfDocument pdf  = new PdfDocument(writer);
            Document doc     = new Document(pdf);

            PdfFont bold    = PdfFontFactory.createFont("Helvetica-Bold");
            PdfFont regular = PdfFontFactory.createFont("Helvetica");

            doc.add(new Paragraph("FINANCEPRO")
                    .setFont(bold).setFontSize(28)
                    .setFontColor(BRAND_BLUE)
                    .setTextAlignment(TextAlignment.CENTER));

            doc.add(new Paragraph("Tax Invoice / Receipt")
                    .setFont(regular).setFontSize(13)
                    .setFontColor(ColorConstants.GRAY)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setMarginBottom(24));

            Table meta = new Table(UnitValue.createPercentArray(new float[]{50, 50})).useAllAvailableWidth();
            meta.addCell(cell("Invoice No.", bold, true));
            meta.addCell(cell(payment.getInvoiceNumber() != null ? payment.getInvoiceNumber() : "N/A", regular, false));
            meta.addCell(cell("Date", bold, true));
            meta.addCell(cell(payment.getPaidAt() != null ? payment.getPaidAt().format(FMT) : "N/A", regular, false));
            meta.addCell(cell("Customer", bold, true));
            meta.addCell(cell(payment.getUser().getFullName(), regular, false));
            meta.addCell(cell("Email", bold, true));
            meta.addCell(cell(payment.getUser().getEmail(), regular, false));
            meta.addCell(cell("Status", bold, true));
            meta.addCell(cell(payment.getStatus().name(), regular, false));
            doc.add(meta);
            doc.add(new Paragraph("\n"));

            Table items = new Table(UnitValue.createPercentArray(new float[]{50, 25, 25})).useAllAvailableWidth();
            for (String h : new String[]{"Description", "Billing Cycle", "Amount"}) {
                items.addHeaderCell(new Cell().add(new Paragraph(h).setFont(bold).setFontSize(11))
                        .setBackgroundColor(BRAND_BLUE).setFontColor(ColorConstants.WHITE).setPadding(8));
            }
            items.addCell(new Cell().add(new Paragraph(
                    payment.getPlan() != null ? "FinancePro " + payment.getPlan().getName() + " Plan" : "Plan")
                    .setFont(regular).setFontSize(11)).setPadding(8).setBackgroundColor(LIGHT_GRAY));
            items.addCell(new Cell().add(new Paragraph(payment.getBillingCycle() != null ? payment.getBillingCycle() : "")
                    .setFont(regular).setFontSize(11)).setPadding(8).setBackgroundColor(LIGHT_GRAY));
            items.addCell(new Cell().add(new Paragraph("₹" + payment.getAmount().toPlainString())
                    .setFont(bold).setFontSize(11)).setPadding(8).setBackgroundColor(LIGHT_GRAY));
            doc.add(items);
            doc.add(new Paragraph("\n"));

            doc.add(new Paragraph("Total Paid: ₹" + payment.getAmount().toPlainString())
                    .setFont(bold).setFontSize(16)
                    .setFontColor(BRAND_BLUE)
                    .setTextAlignment(TextAlignment.RIGHT));

            doc.add(new Paragraph("\n\nThank you for choosing FinancePro!\nsupport@financepro.app")
                    .setFont(regular).setFontSize(10)
                    .setFontColor(ColorConstants.GRAY)
                    .setTextAlignment(TextAlignment.CENTER));

            doc.close();
            return baos.toByteArray();
        } catch (Exception e) {
            log.error("PDF generation failed for payment {}: {}", paymentId, e.getMessage());
            throw new RuntimeException("Failed to generate invoice PDF", e);
        }
    }

    private Cell cell(String text, PdfFont font, boolean isLabel) {
        return new Cell().add(new Paragraph(text).setFont(font).setFontSize(11))
                .setPadding(8)
                .setBackgroundColor(isLabel ? LIGHT_GRAY : ColorConstants.WHITE);
    }
}