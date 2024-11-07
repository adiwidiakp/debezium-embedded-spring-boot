package com.cdc.traccar.repository;

import com.cdc.traccar.model.TcEvents;
import org.springframework.data.repository.CrudRepository;

public interface TcEventsRepo extends CrudRepository<TcEvents,Integer> {
}
