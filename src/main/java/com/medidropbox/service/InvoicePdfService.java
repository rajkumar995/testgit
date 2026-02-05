package com.medidropbox.service;

import com.medidropbox.entity.Booking;

/**
 * Generates invoice PDF for a booking with payment.
 */
public interface InvoicePdfService {

    /**
     * Generate invoice PDF bytes for the given booking (must have payment).
     *
     * @param booking booking with payment, doctor, hospital, patient loaded
     * @return PDF as byte array
     */
    byte[] generateInvoicePdf(Booking booking);
}
