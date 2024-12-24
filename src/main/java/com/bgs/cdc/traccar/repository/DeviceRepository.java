package com.bgs.cdc.traccar.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.bgs.cdc.traccar.domain.TcDevice;

@Repository
public interface DeviceRepository extends JpaRepository<TcDevice, Long> {
    @Query(nativeQuery = true, value = "SELECT id, name FROM tc_devices WHERE id = ?1")
    List<Object[]> findByDeviceId(Long deviceId);
}
