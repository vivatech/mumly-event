package com.vivatech.mumly_event.helper;

public class MumlyEnums {
    public enum EventStatus {
        SUCCESS, FAILED, PENDING, ACTIVE, COMPLETED, CANCELLED
    }

    public enum PaymentAggregator {
        SAFARI, WAAFI
    }

    public enum PaymentMode {
        CASH, DIGITAL_WALLET, BNPL, BANK_TRANSFER, MOBILE_MONEY, CARD
    }

    public enum PaymentStatus {
        SUCCESS, FAILED, PENDING, CANCELLED
    }

}
