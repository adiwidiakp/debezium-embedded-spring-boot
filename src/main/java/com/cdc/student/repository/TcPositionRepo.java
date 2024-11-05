package com.cdc.student.repository;

import com.cdc.student.model.TcPosition;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TcPositionRepo extends CrudRepository<TcPosition, Integer> {
}
