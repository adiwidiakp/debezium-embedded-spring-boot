package com.bgs.cdc.traccar.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.bgs.cdc.traccar.domain.TcDevice;

@Repository
public interface DeviceRepository extends JpaRepository<TcDevice, Long> {

    TcDevice findOneById(Long id);
}
