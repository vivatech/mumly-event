package com.vivatech.mumly_event.repository;

import com.vivatech.mumly_event.model.EventRegistration;
import com.vivatech.mumly_event.model.MumlyEventPayment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MumlyEventPaymentRepository extends JpaRepository<MumlyEventPayment, Integer> {
    MumlyEventPayment findByReferenceNo(String referenceNo);

    MumlyEventPayment findByEventRegistration(EventRegistration registration);

    List<MumlyEventPayment> findByEventRegistrationSelectedEventId(Integer eventId);

    MumlyEventPayment findByTransactionId(String transactionId);

    List<MumlyEventPayment> findByEventRegistrationSelectedEventIdAndPaymentStatus(Integer eventId, String paymentStatus);
}
