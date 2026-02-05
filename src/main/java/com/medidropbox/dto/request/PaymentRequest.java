package com.medidropbox.dto.request;

import com.medidropbox.enums.PaymentMode;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PaymentRequest {
    @NotNull(message = "Payment mode is required")
    private PaymentMode paymentMode;
    
    @NotNull(message = "Total bill is required")
    private BigDecimal totalBill;
    
    @NotNull(message = "Total amount is required")
    private BigDecimal totalAmount;
    
    private BigDecimal discount = BigDecimal.ZERO;
    
    private BigDecimal taxableAmount;
    
    private BigDecimal gst;
    
    /**
     * Transaction ID from payment gateway (optional)
     * e.g., UPI transaction ID, card transaction ID, payment gateway reference number
     */
    private String transactionId;
}
