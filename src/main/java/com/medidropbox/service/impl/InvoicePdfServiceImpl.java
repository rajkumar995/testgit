package com.medidropbox.service.impl;

import com.medidropbox.entity.Address;
import com.medidropbox.entity.Booking;
import com.medidropbox.entity.Hospital;
import com.medidropbox.entity.HospitalSettings;
import com.medidropbox.entity.Payment;
import com.medidropbox.service.HospitalSettingsService;
import com.medidropbox.service.InvoicePdfService;
import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import org.springframework.stereotype.Service;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/**
 * Premium invoice PDF matching the premium hospital invoice HTML layout:
 * 3-column header (hospital left, logo center, invoice right), patient section with avatar,
 * billing table, QR section, footer with signature and barcode.
 */
@Service
public class InvoicePdfServiceImpl implements InvoicePdfService {

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("MMM dd, yyyy", Locale.ENGLISH);
    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("hh:mm a", Locale.ENGLISH);
    // Match HTML template colors
    private static final Color GRAY_TEXT = new Color(85, 85, 85);           // #555
    private static final Color GRAY_BORDER = new Color(234, 234, 234);      // #eaeaea
    private static final Color TABLE_HEADER_BG = new Color(238, 241, 247); // #eef1f7
    private static final Color INV_NUMBER_BG = new Color(241, 243, 247);   // #f1f3f7
    private static final Color TOTAL_GREEN = new Color(27, 127, 90);        // #1b7f5a
    private static final Color QR_SECTION_BG = new Color(247, 249, 252);   // #f7f9fc
    private static final Color PAID_BG = new Color(230, 247, 239);         // #e6f7ef
    private static final Color PAID_TEXT = new Color(25, 135, 84);         // #198754
    private static final Color ROW_BORDER = new Color(238, 238, 238);      // #eee

    private final HospitalSettingsService hospitalSettingsService;

    public InvoicePdfServiceImpl(HospitalSettingsService hospitalSettingsService) {
        this.hospitalSettingsService = hospitalSettingsService;
    }

    @Override
    public byte[] generateInvoicePdf(Booking booking) {
        if (booking.getPayment() == null) {
            throw new IllegalArgumentException("Booking must have payment to generate invoice");
        }
        Payment payment = booking.getPayment();
        Hospital hospital = booking.getHospital();
        HospitalSettings settings = null;
        try {
            settings = hospital != null ? hospitalSettingsService.getSettingsEntity(hospital.getId()) : null;
        } catch (Exception ignored) {
            // Use null settings
        }

        Document document = new Document(PageSize.A4, 36, 36, 36, 36);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            PdfWriter writer = PdfWriter.getInstance(document, baos);
            document.open();

            Font fontTitle = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18, Color.BLACK);
            Font fontHeader = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, Color.BLACK);
            Font fontSub = FontFactory.getFont(FontFactory.HELVETICA, 11, GRAY_TEXT);
            Font fontNormal = FontFactory.getFont(FontFactory.HELVETICA, 11, Color.BLACK);
            Font fontPatientName = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16, Color.BLACK);
            Font fontTotal = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14, TOTAL_GREEN);
            Font fontPaid = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11, PAID_TEXT);

            // ----- HEADER: 3 columns (hospital-left | logo-center | invoice-right) -----
            PdfPTable headerTable = new PdfPTable(3);
            headerTable.setWidthPercentage(100f);
            headerTable.setWidths(new float[]{1.2f, 1f, 1.2f});
            headerTable.setSpacingAfter(20);
            headerTable.getDefaultCell().setBorder(Rectangle.NO_BORDER);

            // Left: Hospital address, phone
            String addressLine = formatAddress(hospital);
            String phone = hospital != null && hospital.getBookingCallNumber() != null ? hospital.getBookingCallNumber() : "";
            Phrase leftPhrase = new Phrase();
            if (addressLine != null && !addressLine.isEmpty()) {
                leftPhrase.add(new Chunk(addressLine + "\n", fontSub));
            }
            if (phone != null && !phone.isEmpty()) {
                leftPhrase.add(new Chunk(phone + "\n", fontSub));
            }
            PdfPCell leftCell = new PdfPCell(leftPhrase);
            leftCell.setBorder(Rectangle.NO_BORDER);
            leftCell.setVerticalAlignment(Element.ALIGN_TOP);
            leftCell.setPadding(0);
            headerTable.addCell(leftCell);

            // Center: Logo + Hospital name
            PdfPTable centerBlock = new PdfPTable(1);
            centerBlock.setWidthPercentage(100f);
            centerBlock.getDefaultCell().setBorder(Rectangle.NO_BORDER);
            centerBlock.getDefaultCell().setHorizontalAlignment(Element.ALIGN_CENTER);
            boolean logoUsed = false;
            if (hospital != null && hospital.getImages() != null && !hospital.getImages().isEmpty()) {
                try {
                    String logoUrl = hospital.getImages().get(0);
                    if (logoUrl != null && !logoUrl.isBlank()) {
                        Image img = Image.getInstance(new URL(logoUrl));
                        img.scaleToFit(70, 70);
                        centerBlock.addCell(createCenteredCell(img));
                        logoUsed = true;
                    }
                } catch (Exception ignored) {
                    // Fall back to text
                }
            }
            if (!logoUsed) {
                centerBlock.addCell(createCenteredCell(new Paragraph(" ", fontNormal)));
            }
            String hName = hospital != null ? hospital.getName().toUpperCase(Locale.ENGLISH) : "HOSPITAL";
            centerBlock.addCell(createCenteredCell(new Paragraph(hName, fontHeader)));
            PdfPCell centerCell = new PdfPCell(centerBlock);
            centerCell.setBorder(Rectangle.NO_BORDER);
            centerCell.setHorizontalAlignment(Element.ALIGN_CENTER);
            centerCell.setVerticalAlignment(Element.ALIGN_TOP);
            centerCell.setPadding(0);
            headerTable.addCell(centerCell);

            // Right: INVOICE, #INV-xxx, Queue no.
            PdfPTable rightBlock = new PdfPTable(1);
            rightBlock.setWidthPercentage(100f);
            rightBlock.getDefaultCell().setBorder(Rectangle.NO_BORDER);
            rightBlock.setHorizontalAlignment(Element.ALIGN_RIGHT);
            rightBlock.addCell(createNoBorderCell(new Paragraph("INVOICE", fontTitle), Element.ALIGN_RIGHT));
            String invNum = "#INV-" + String.format("%05d", booking.getId());
            PdfPCell invCell = new PdfPCell(new Paragraph(invNum, fontSub));
            invCell.setBorder(Rectangle.NO_BORDER);
            invCell.setBackgroundColor(INV_NUMBER_BG);
            invCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
            invCell.setPadding(6);
            invCell.setPaddingRight(12);
            invCell.setPaddingLeft(12);
            rightBlock.addCell(invCell);
            rightBlock.addCell(createNoBorderCell(new Paragraph(" ", fontNormal), Element.ALIGN_RIGHT));
            if (booking.getQueue() != null) {
                rightBlock.addCell(createNoBorderCell(
                    new Paragraph("Queue no. " + (char) 35 + booking.getQueue().getQueueNumber(), fontSub),
                    Element.ALIGN_RIGHT));
            }
            PdfPCell rightCell = new PdfPCell(rightBlock);
            rightCell.setBorder(Rectangle.NO_BORDER);
            rightCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
            rightCell.setVerticalAlignment(Element.ALIGN_TOP);
            rightCell.setPadding(0);
            headerTable.addCell(rightCell);
            document.add(headerTable);

            // ----- Patient section: avatar + name/phone | dates -----
            String patientName = booking.getHospitalPatient() != null && booking.getHospitalPatient().getFullName() != null
                ? booking.getHospitalPatient().getFullName()
                : (booking.getGlobalPatient() != null ? booking.getGlobalPatient().getFullName() : "\u2014");
            String patientPhone = booking.getHospitalPatient() != null && booking.getHospitalPatient().getPhone() != null
                ? booking.getHospitalPatient().getPhone()
                : (booking.getPhoneNumber() != null ? booking.getPhoneNumber() : "");

            PdfPTable patientTable = new PdfPTable(2);
            patientTable.setWidthPercentage(100f);
            patientTable.setWidths(new float[]{1f, 1f});
            patientTable.setSpacingAfter(25);
            patientTable.getDefaultCell().setBorder(Rectangle.NO_BORDER);
            patientTable.getDefaultCell().setVerticalAlignment(Element.ALIGN_MIDDLE);

            // Left: Avatar circle (gray circle with initial) + name + phone
            PdfPTable patientLeft = new PdfPTable(2);
            patientLeft.setWidthPercentage(100f);
            patientLeft.setWidths(new float[]{0.22f, 0.78f});
            patientLeft.getDefaultCell().setBorder(Rectangle.NO_BORDER);
            patientLeft.getDefaultCell().setVerticalAlignment(Element.ALIGN_MIDDLE);
            PdfPCell avatarCell = new PdfPCell(new Paragraph(patientName.isEmpty() ? "P" : String.valueOf(patientName.charAt(0)).toUpperCase(Locale.ENGLISH), FontFactory.getFont(FontFactory.HELVETICA_BOLD, 22, GRAY_TEXT)));
            avatarCell.setBorder(Rectangle.NO_BORDER);
            avatarCell.setBackgroundColor(new Color(234, 238, 245)); // #eaeef5
            avatarCell.setHorizontalAlignment(Element.ALIGN_CENTER);
            avatarCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
            avatarCell.setFixedHeight(50);
            avatarCell.setPadding(8);
            patientLeft.addCell(avatarCell);
            Phrase namePhrase = new Phrase();
            namePhrase.add(new Chunk(patientName + "\n", fontPatientName));
            if (patientPhone != null && !patientPhone.isEmpty()) {
                namePhrase.add(new Chunk("(" + patientPhone + ")", fontSub));
            }
            patientLeft.addCell(createNoBorderCell(new Paragraph(namePhrase), Element.ALIGN_LEFT));
            patientTable.addCell(patientLeft);

            // Right: Invoice date, Booking Date
            Phrase datePhrase = new Phrase();
            if (payment.getPaymentDate() != null) {
                datePhrase.add(new Chunk(payment.getPaymentDate().format(DATE_FMT) + "\n", fontNormal));
            }
            datePhrase.add(new Chunk("Booking Date: ", fontSub));
            if (booking.getBookingDate() != null) {
                datePhrase.add(new Chunk(booking.getBookingDate().format(DATE_FMT), fontNormal));
            }
            patientTable.addCell(createNoBorderCell(new Paragraph(datePhrase), Element.ALIGN_RIGHT));
            document.add(patientTable);

            // ----- Billing table -----
            PdfPTable billTable = new PdfPTable(2);
            billTable.setWidthPercentage(100f);
            billTable.setWidths(new float[]{3f, 1f});
            billTable.setSpacingBefore(4);
            billTable.setSpacingAfter(30);
            addBillingHeader(billTable, fontHeader);
            addBillingRow(billTable, "Doctor Fees", payment.getTotalBill(), fontNormal);
            if (payment.getGst() != null && payment.getGst().compareTo(BigDecimal.ZERO) > 0) {
                addBillingRow(billTable, "GST (18%)", payment.getGst(), fontNormal);
            }
            BigDecimal taxable = payment.getTaxableAmount();
            if (taxable != null && taxable.compareTo(BigDecimal.ZERO) > 0) {
                BigDecimal hospitalGst = taxable.multiply(new BigDecimal("0.12")).setScale(2, RoundingMode.HALF_UP);
                if (hospitalGst.compareTo(BigDecimal.ZERO) > 0) {
                    addBillingRow(billTable, "Hospital GST (12%)", hospitalGst, fontNormal);
                }
            }
            if (payment.getDiscount() != null && payment.getDiscount().compareTo(BigDecimal.ZERO) > 0) {
                addBillingRow(billTable, "Discount", payment.getDiscount().negate(), fontNormal);
            }
            PdfPCell totalDesc = new PdfPCell(new Phrase("TOTAL AMOUNT", fontHeader));
            totalDesc.setPadding(14);
            totalDesc.setBorderWidth(0.5f);
            totalDesc.setBorderColor(ROW_BORDER);
            totalDesc.setBorderWidthTop(0.5f);
            billTable.addCell(totalDesc);
            PdfPCell totalAmt = new PdfPCell(new Phrase("\u20B9" + formatAmount(payment.getTotalAmount()), fontTotal));
            totalAmt.setPadding(14);
            totalAmt.setHorizontalAlignment(Element.ALIGN_RIGHT);
            totalAmt.setBorderWidth(0.5f);
            totalAmt.setBorderColor(ROW_BORDER);
            totalAmt.setBorderWidthTop(0.5f);
            billTable.addCell(totalAmt);
            document.add(billTable);

            // ----- QR section: gray bg, QR left | Scan to pay, Payment Method, Paid, date right -----
            PdfPTable qrSection = new PdfPTable(2);
            qrSection.setWidthPercentage(100f);
            qrSection.setWidths(new float[]{0.35f, 0.65f});
            qrSection.setSpacingAfter(30);
            qrSection.getDefaultCell().setBorder(Rectangle.NO_BORDER);
            qrSection.getDefaultCell().setVerticalAlignment(Element.ALIGN_MIDDLE);
            qrSection.getDefaultCell().setBackgroundColor(QR_SECTION_BG);
            qrSection.setSpacingBefore(0);
            qrSection.setSpacingAfter(30);

            String qrImageUrl = settings != null ? settings.getPaymentQrImageUrl() : null;
            String qrData = "INV-" + String.format("%05d", booking.getId()) + "-" + formatAmount(payment.getTotalAmount());
            if (qrImageUrl != null && !qrImageUrl.isBlank()) {
                try {
                    Image qrImg = Image.getInstance(new URL(qrImageUrl));
                    qrImg.scaleToFit(120, 120);
                    PdfPCell qrCell = new PdfPCell(qrImg, true);
                    qrCell.setBorder(Rectangle.NO_BORDER);
                    qrCell.setBackgroundColor(QR_SECTION_BG);
                    qrCell.setPadding(10);
                    qrSection.addCell(qrCell);
                } catch (Exception ignored) {
                    addQrPlaceholderCell(qrSection, qrData, fontSub);
                }
            } else {
                try {
                    String qrApiUrl = "https://api.qrserver.com/v1/create-qr-code/?size=120x120&data=" + java.net.URLEncoder.encode(qrData, StandardCharsets.UTF_8);
                    Image qrImg = Image.getInstance(new URL(qrApiUrl));
                    qrImg.scaleToFit(120, 120);
                    PdfPCell qrCell = new PdfPCell(qrImg, true);
                    qrCell.setBorder(Rectangle.NO_BORDER);
                    qrCell.setBackgroundColor(QR_SECTION_BG);
                    qrCell.setPadding(10);
                    qrSection.addCell(qrCell);
                } catch (Exception ignored) {
                    addQrPlaceholderCell(qrSection, qrData, fontSub);
                }
            }

            PdfPTable statusBlock = new PdfPTable(1);
            statusBlock.setWidthPercentage(100f);
            statusBlock.getDefaultCell().setBorder(Rectangle.NO_BORDER);
            statusBlock.setHorizontalAlignment(Element.ALIGN_RIGHT);
            statusBlock.addCell(createNoBorderCell(new Paragraph("Scan QR Code to pay", fontSub), Element.ALIGN_RIGHT));
            statusBlock.addCell(createNoBorderCell(new Paragraph("Payment Method", fontSub), Element.ALIGN_RIGHT));
            statusBlock.addCell(createNoBorderCell(new Paragraph(payment.getPaymentMode() != null ? payment.getPaymentMode().name() : "\u2014", fontNormal), Element.ALIGN_RIGHT));
            PdfPCell paidCell = new PdfPCell(new Phrase("Paid", fontPaid));
            paidCell.setBorder(Rectangle.NO_BORDER);
            paidCell.setBackgroundColor(PAID_BG);
            paidCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
            paidCell.setPadding(6);
            paidCell.setPaddingLeft(14);
            paidCell.setPaddingRight(14);
            statusBlock.addCell(paidCell);
            if (payment.getPaymentDate() != null) {
                statusBlock.addCell(createNoBorderCell(
                    new Paragraph(payment.getPaymentDate().format(DATE_FMT) + " \u2022 " + payment.getPaymentDate().format(TIME_FMT), fontSub),
                    Element.ALIGN_RIGHT));
            }
            PdfPCell statusCell = new PdfPCell(statusBlock);
            statusCell.setBorder(Rectangle.NO_BORDER);
            statusCell.setBackgroundColor(QR_SECTION_BG);
            statusCell.setVerticalAlignment(Element.ALIGN_TOP);
            statusCell.setPadding(10);
            qrSection.addCell(statusCell);
            document.add(qrSection);

            // ----- Footer: thank you left | signature line + doctor right -----
            String hospitalName = hospital != null ? hospital.getName() : "us";
            Paragraph thankYou = new Paragraph(
                "Thank you for booking with " + hospitalName + ".\nPlease bring this pay slip with you.",
                fontSub);
            thankYou.setSpacingAfter(0);

            PdfPTable footerTable = new PdfPTable(2);
            footerTable.setWidthPercentage(100f);
            footerTable.setWidths(new float[]{1f, 1f});
            footerTable.getDefaultCell().setBorder(Rectangle.NO_BORDER);
            footerTable.getDefaultCell().setVerticalAlignment(Element.ALIGN_TOP);
            footerTable.addCell(createNoBorderCell(thankYou, Element.ALIGN_LEFT));

            PdfPTable sigBlock = new PdfPTable(1);
            sigBlock.setTotalWidth(150);
            sigBlock.setLockedWidth(true);
            sigBlock.getDefaultCell().setBorder(Rectangle.NO_BORDER);
            sigBlock.setHorizontalAlignment(Element.ALIGN_RIGHT);
            sigBlock.addCell(createNoBorderCell(new Paragraph(" ", fontNormal), Element.ALIGN_RIGHT));
            sigBlock.addCell(createNoBorderCell(new Paragraph(" ", fontNormal), Element.ALIGN_RIGHT));
            PdfPCell sigLineCell = new PdfPCell();
            sigLineCell.setBorder(Rectangle.TOP);
            sigLineCell.setBorderWidth(1f);
            sigLineCell.setFixedHeight(1f);
            sigLineCell.setPadding(0);
            sigBlock.addCell(sigLineCell);
            String doctorName = booking.getDoctor() != null ? booking.getDoctor().getName() : "\u2014";
            if (doctorName != null && !doctorName.trim().startsWith("Dr.") && !doctorName.trim().startsWith("Dr ")) {
                doctorName = "Dr. " + doctorName.trim();
            }
            sigBlock.addCell(createNoBorderCell(new Paragraph(doctorName, fontHeader), Element.ALIGN_RIGHT));
            String doctorTitle = booking.getDoctor() != null && booking.getDoctor().getTitle() != null
                ? booking.getDoctor().getTitle()
                : (booking.getDoctor() != null && booking.getDoctor().getSpecialty() != null ? booking.getDoctor().getSpecialty() : "");
            if (doctorTitle != null && !doctorTitle.isEmpty()) {
                sigBlock.addCell(createNoBorderCell(new Paragraph(doctorTitle, fontSub), Element.ALIGN_RIGHT));
            }
            PdfPCell sigCell = new PdfPCell(sigBlock);
            sigCell.setBorder(Rectangle.NO_BORDER);
            sigCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
            sigCell.setPadding(0);
            sigCell.setPaddingLeft(80);
            footerTable.addCell(sigCell);
            document.add(footerTable);

            // ----- Barcode at bottom -----
            document.add(new Paragraph(" ", fontNormal));
            float barY = document.getPageSize().getBottom() + 45;
            com.lowagie.text.pdf.PdfContentByte cb = writer.getDirectContent();
            cb.saveState();
            cb.setColorFill(Color.BLACK);
            float barLeft = 36;
            float barRight = document.getPageSize().getRight() - 36;
            float barHeight = 40;
            for (float x = barLeft; x < barRight; x += 6) {
                cb.rectangle(x, barY, 2, barHeight);
                cb.fill();
            }
            cb.restoreState();

            document.close();
            return baos.toByteArray();
        } catch (DocumentException e) {
            throw new RuntimeException("Failed to generate invoice PDF", e);
        }
    }

    private void addQrPlaceholderCell(PdfPTable qrSection, String qrData, Font fontSub) {
        PdfPCell scanCell = new PdfPCell(new Paragraph("Scan QR Code to pay\n" + qrData, fontSub));
        scanCell.setBorder(Rectangle.NO_BORDER);
        scanCell.setBackgroundColor(QR_SECTION_BG);
        scanCell.setPadding(10);
        qrSection.addCell(scanCell);
    }

    private PdfPCell createCenteredCell(Element content) {
        PdfPCell cell = new PdfPCell();
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.addElement(content);
        return cell;
    }

    private String formatAddress(Hospital hospital) {
        if (hospital == null || hospital.getAddress() == null) return "";
        Address a = hospital.getAddress();
        StringBuilder sb = new StringBuilder();
        if (a.getAddressLine1() != null) sb.append(a.getAddressLine1());
        if (a.getAddressLine2() != null && !a.getAddressLine2().isBlank()) sb.append(", ").append(a.getAddressLine2());
        if (a.getCity() != null) sb.append(", ").append(a.getCity());
        if (a.getState() != null) sb.append(", ").append(a.getState());
        if (a.getPincode() != null) sb.append("- ").append(a.getPincode());
        return sb.toString();
    }

    private PdfPCell createNoBorderCell(Element content, int alignment) {
        PdfPCell cell = new PdfPCell();
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setHorizontalAlignment(alignment);
        cell.addElement(content);
        return cell;
    }

    private void addBillingHeader(PdfPTable table, Font font) {
        PdfPCell desc = new PdfPCell(new Phrase("Description", font));
        desc.setBackgroundColor(TABLE_HEADER_BG);
        desc.setPadding(14);
        desc.setBorderWidth(0.5f);
        desc.setBorderColor(GRAY_BORDER);
        table.addCell(desc);
        PdfPCell amt = new PdfPCell(new Phrase("Amount", font));
        amt.setBackgroundColor(TABLE_HEADER_BG);
        amt.setPadding(14);
        amt.setBorderWidth(0.5f);
        amt.setBorderColor(GRAY_BORDER);
        amt.setHorizontalAlignment(Element.ALIGN_RIGHT);
        table.addCell(amt);
    }

    private void addBillingRow(PdfPTable table, String desc, BigDecimal amount, Font font) {
        PdfPCell descCell = new PdfPCell(new Phrase(desc, font));
        descCell.setPadding(14);
        descCell.setBorderWidth(0.5f);
        descCell.setBorderColor(ROW_BORDER);
        table.addCell(descCell);
        PdfPCell amtCell = new PdfPCell(new Phrase("\u20B9" + formatAmount(amount), font));
        amtCell.setPadding(14);
        amtCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        amtCell.setBorderWidth(0.5f);
        amtCell.setBorderColor(ROW_BORDER);
        table.addCell(amtCell);
    }

    private static String formatAmount(BigDecimal amount) {
        if (amount == null) return "0.00";
        return String.format("%.2f", amount);
    }
}
