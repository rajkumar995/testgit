package com.medidropbox.enums;

/**
 * Queue status enum with strict transition rules
 */
public enum QueueStatus {
    WAITING,
    CALLED,
    SERVING,
    SERVED,
    CANCELLED,
    NO_SHOW
}
