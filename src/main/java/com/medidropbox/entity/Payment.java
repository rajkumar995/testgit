package com.medidropbox.entity;

import com.medidropbox.enums.PaymentMode;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Payment entity
 */
@Entity
@Table(name = "md_payments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Payment extends BaseAuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "Payment date is required")
    @Column(name = "payment_date", nullable = false)
    private LocalDateTime paymentDate;

    @NotNull(message = "Payment mode is required")
    @Enumerated(EnumType.STRING)
    @Column(name = "payment_mode", nullable = false)
    private PaymentMode paymentMode;

    @NotNull(message = "Total bill is required")
    @Column(name = "total_bill", nullable = false, precision = 10, scale = 2)
    private BigDecimal totalBill;

    @NotNull(message = "Total amount is required")
    @Column(name = "total_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal totalAmount;

    @Column(name = "discount", precision = 10, scale = 2)
    private BigDecimal discount = BigDecimal.ZERO;

    @Column(name = "taxable_amount", precision = 10, scale = 2)
    private BigDecimal taxableAmount;

    @Column(name = "gst", precision = 10, scale = 2)
    private BigDecimal gst;
    
    /**
     * Transaction ID from payment gateway (optional)
     * e.g., UPI transaction ID, card transaction ID, etc.
     */
    @Column(name = "transaction_id")
    private String transactionId;

    /**
     * S3 URL of the generated invoice PDF (set after booking/payment creation).
     */
    @Column(name = "invoice_url", length = 1024)
    private String invoiceUrl;
}
