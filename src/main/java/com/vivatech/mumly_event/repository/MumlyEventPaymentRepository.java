package com.vivatech.mumly_event.repository;

import com.vivatech.mumly_event.model.EventRegistration;
import com.vivatech.mumly_event.model.MumlyEventPayment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MumlyEventPaymentRepository extends JpaRepository<MumlyEventPayment, Integer> {
    MumlyEventPayment findByReferenceNo(String referenceNo);

    MumlyEventPayment findByEventRegistration(EventRegistration registration);

    MumlyEventPayment findByTransactionId(String transactionId);

}
