package com.bgs.cdc.traccar.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.bgs.cdc.traccar.domain.EventRitase;

@Repository
public interface EventRitaseRepository extends JpaRepository<EventRitase, Long> {
}
