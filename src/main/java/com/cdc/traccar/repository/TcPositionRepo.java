package com.cdc.traccar.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import com.cdc.traccar.model.TcPosition;

@Repository
public interface TcPositionRepo extends CrudRepository<TcPosition, Integer> {
}
