package com.bgs.cdc.traccar.service;

import com.bgs.cdc.traccar.domain.TcDevice;
import com.bgs.cdc.traccar.domain.TcEvent;
import com.bgs.cdc.traccar.domain.TcGeofence;
import com.bgs.cdc.traccar.domain.TcPosition;
import com.bgs.cdc.traccar.repository.DeviceRepository;
import com.bgs.cdc.traccar.repository.EventRepository;
import com.bgs.cdc.traccar.repository.GeofenceRepository;
import com.bgs.cdc.traccar.repository.PositionRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.debezium.data.Envelope.Operation;

import org.springframework.stereotype.Service;

import java.util.Map;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class TraccarService {

    private final PositionRepository positionRepository;
    private final EventRepository eventsRepository;
    private final DeviceRepository devicesRepository;
    private final GeofenceRepository geofencesRepository;

    private static final String tc_positions = "tc_positions";
    private static final String tc_events = "tc_events";
    private static final String tc_devices = "tc_devices";
    private static final String tc_geofences = "tc_geofences";

    public void replicateData(String table, Map<String, Object> data, Operation operation) {
        if (Operation.DELETE == operation) {
            if (tc_devices.equals(table)) {
               final ObjectMapper mapper = new ObjectMapper();
               final TcDevice device = mapper.convertValue(data, TcDevice.class);
               log.info("{} - {} => {}", tc_devices, operation, data);
               devicesRepository.deleteById(device.getId());
           } else if (tc_geofences.equals(table)) {
               final ObjectMapper mapper = new ObjectMapper();
               final TcGeofence geofence = mapper.convertValue(data, TcGeofence.class);
               log.info("{} - {} => {}", tc_geofences, operation, data);
               geofencesRepository.deleteById(geofence.getId());
           }
        } else if (Operation.CREATE == operation || Operation.UPDATE == operation) {
            if (tc_positions.equals(table) && Operation.CREATE == operation) {
                final ObjectMapper mapper = new ObjectMapper();
                final TcPosition position = mapper.convertValue(data, TcPosition.class);
                log.info("{} - {} => {}", tc_positions, operation, data);
                positionRepository.save(position);
            } else if (tc_events.equals(table) && Operation.CREATE == operation) {
                final ObjectMapper mapper = new ObjectMapper();
                final TcEvent event = mapper.convertValue(data, TcEvent.class);
                log.info("{} - {} => {}", tc_events, operation, data);
                eventsRepository.save(event);
            } else if (tc_devices.equals(table)) {
                final ObjectMapper mapper = new ObjectMapper();
                final TcDevice device = mapper.convertValue(data, TcDevice.class);
                log.info("{} - {} => {}", tc_devices, operation, data);
                devicesRepository.save(device);
            } else if (tc_geofences.equals(table)) {
                final ObjectMapper mapper = new ObjectMapper();
                final TcGeofence geofence = mapper.convertValue(data, TcGeofence.class);
                log.info("{} - {} => {}", tc_geofences, operation, data);
                geofencesRepository.save(geofence);
            }
        }  
    }

    public String getNameTcDeviceById(Long id) {
        TcDevice tcDevice = devicesRepository.findOneById(id);

        return tcDevice.getName().replace(" ", "");
    }
}