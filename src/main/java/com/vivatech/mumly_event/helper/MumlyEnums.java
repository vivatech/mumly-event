package com.vivatech.mumly_event.helper;

public class MumlyEnums {
    public enum EventStatus {
        SUCCESS, FAILED, PENDING, ACTIVE, COMPLETED, CANCELLED, APPROVE, REJECT, REFUND
    }

    public enum PaymentAggregator {
        SAFARI, WAAFI
    }

    public enum PaymentMode {
        CASH, BANK_TRANSFER, MPESA, INTA_SEND, SAFARI_COM
    }

    public enum PaymentStatus {
        SUCCESS, FAILED, PENDING, CANCELLED, REFUND, REFUND_FAILED, COMPLETE
    }

    public enum PaymentBreakUp {
        GROSS_REVENUE, COMMISSION, NET_REVENUE
    }

    public enum NotificationType {
        REGISTRATION, EMERGENCY, PAYMENT, FEEDBACK
    }

    public enum ApplicationName {
        MUMLY_EVENT, MUMLY_TUTOR
    }

}
