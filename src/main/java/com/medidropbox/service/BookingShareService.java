package com.medidropbox.service;

import com.medidropbox.dto.request.BookingShareRequest;
import com.medidropbox.dto.response.BookingShareResponse;
import com.medidropbox.dto.response.ShareUrlResponse;

public interface BookingShareService {
    ShareUrlResponse shareBooking(BookingShareRequest request, Long globalPatientId);
    BookingShareResponse getSharedBooking(String shareToken);
    BookingShareResponse getSharedBookingByShortCode(String shortCode);
    void revokeShare(Long shareId, Long globalPatientId);
}
