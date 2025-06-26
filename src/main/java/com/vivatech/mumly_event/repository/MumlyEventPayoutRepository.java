package com.vivatech.mumly_event.repository;

import com.vivatech.mumly_event.model.MumlyEvent;
import com.vivatech.mumly_event.model.MumlyEventPayout;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MumlyEventPayoutRepository extends JpaRepository<MumlyEventPayout, Integer> {

    MumlyEventPayout findByEventId(Integer eventId);
    MumlyEventPayout findByEventIdAndPaymentStatusIn(Integer eventId, List<String> paymentStatus);

    List<MumlyEventPayout> findByEventIn(List<MumlyEvent> eventList);

    List<MumlyEventPayout> findByPaymentStatus(String paymentStatus);
}
