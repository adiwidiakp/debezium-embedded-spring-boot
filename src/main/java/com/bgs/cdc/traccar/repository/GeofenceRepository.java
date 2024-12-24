package com.bgs.cdc.traccar.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.bgs.cdc.traccar.domain.TcGeofence;

@Repository
public interface GeofenceRepository extends JpaRepository<TcGeofence, Long> {
    @Query(nativeQuery = true, value = "SELECT id, name, attributes FROM tc_geofences WHERE id = ?1")
    List<Object[]> findByGeofenceId(Long geofenceid);
}
