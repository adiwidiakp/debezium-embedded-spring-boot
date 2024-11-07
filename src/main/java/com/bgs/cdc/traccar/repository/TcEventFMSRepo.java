package com.bgs.cdc.traccar.repository;

import org.springframework.data.repository.CrudRepository;

import com.bgs.cdc.traccar.model.TcEventFMS;

public interface TcEventFMSRepo extends CrudRepository<TcEventFMS,Integer> {
}