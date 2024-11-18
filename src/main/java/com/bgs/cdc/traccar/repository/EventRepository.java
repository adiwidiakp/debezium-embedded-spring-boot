package com.bgs.cdc.traccar.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.bgs.cdc.traccar.domain.TcEvent;

@Repository
public interface EventRepository extends JpaRepository<TcEvent, Long> {
}
