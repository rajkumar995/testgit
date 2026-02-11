package com.medidropbox.enums;

/**
 * Type of inventory movement for audit and current stock calculation.
 */
public enum StockMovementType {
    /** Stock received (purchase / restock) */
    RECEIVED,
    /** Stock sold (via bill) */
    SOLD,
    /** Manual adjustment (correction / write-off) */
    ADJUSTMENT
}
