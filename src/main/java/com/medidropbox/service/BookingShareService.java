package com.medidropbox.service;

import com.medidropbox.dto.request.BookingShareRequest;
import com.medidropbox.dto.response.BookingShareResponse;
import com.medidropbox.dto.response.ShareUrlResponse;

import java.util.List;

public interface BookingShareService {
    ShareUrlResponse shareBooking(BookingShareRequest request, Long globalPatientId);
    List<BookingShareResponse> listMyShares(Long globalPatientId);
    /** Single API: pass either 8-char short code or UUID token (Public). */
    BookingShareResponse getSharedBookingByCodeOrToken(String codeOrToken);
    void revokeShare(Long shareId, Long globalPatientId);
}
