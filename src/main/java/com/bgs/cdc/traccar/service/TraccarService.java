package com.bgs.cdc.traccar.service;

import com.bgs.cdc.traccar.model.TcDevice;
import com.bgs.cdc.traccar.model.TcDeviceFMS;
import com.bgs.cdc.traccar.model.TcEvent;
import com.bgs.cdc.traccar.model.TcEventFMS;
import com.bgs.cdc.traccar.model.TcGeofence;
import com.bgs.cdc.traccar.model.TcGeofenceFMS;
import com.bgs.cdc.traccar.model.TcPosition;
import com.bgs.cdc.traccar.model.TcPositionFMS;
import com.bgs.cdc.traccar.repository.TcPositionFMSRepo;
import com.bgs.cdc.traccar.repository.TcEventFMSRepo;
import com.bgs.cdc.traccar.repository.TcDeviceFMSRepo;
import com.bgs.cdc.traccar.repository.TcGeofenceFMSRepo;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.debezium.data.Envelope;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;

@Slf4j
@Service
public class TraccarService {

    @Autowired
    private TcPositionFMSRepo tcPositionFMSRepo;
    @Autowired
    private TcEventFMSRepo tcEventFMSRepo;
    @Autowired
    private TcDeviceFMSRepo tcDeviceFMSRepo;
    @Autowired
    private TcGeofenceFMSRepo tcGeofenceFMSRepo;


    public void maintainReadModel(Map<String, Object> traccarData, String table, Envelope.Operation operation) {
        final ObjectMapper mapper = new ObjectMapper();
        if ("tc_positions".equals(table)) {
            final TcPosition tcPosition = mapper.convertValue(traccarData, TcPosition.class);
            log.info("TraccarService.maintainReadModel - tc_positions : {}", operation);

            TcPositionFMS tcPositionFMS = new TcPositionFMS();
            tcPositionFMS.transformTcPosMasterToSlave(tcPosition);

            try {
                String json = new ObjectMapper().writeValueAsString(tcPositionFMS);
                log.info("TraccarService.maintainReadModel - tc_positions : {}", json);
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }

            if (Envelope.Operation.DELETE.name().equals(operation.name())) {
                tcPositionFMSRepo.deleteById(tcPosition.getId());
            } else {
                tcPositionFMSRepo.save(tcPositionFMS);
            } 
        } else if ("tc_events".equals(table)) {
            final TcEvent tcEvent = mapper.convertValue(traccarData, TcEvent.class);
            log.info("TcPositionService.maintainReadModel - tc_events : {}", operation);

            TcEventFMS tcEventFMS = new TcEventFMS();
            tcEventFMS.transformTcPosMasterToSlave(tcEvent);

            try {
                String json = new ObjectMapper().writeValueAsString(tcEventFMS);
                log.info("TcPositionService.maintainReadModel - tc_events : {}", json);
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }

            if (Envelope.Operation.DELETE.name().equals(operation.name())) {
                tcEventFMSRepo.deleteById(tcEvent.getId());
            } else {
                tcEventFMSRepo.save(tcEventFMS);
            } 
        } else if ("tc_devices".equals(table)) {
            final TcDevice tcDevice = mapper.convertValue(traccarData, TcDevice.class);
            log.info("TcPositionService.maintainReadModel - tc_devices : {}", operation);

            TcDeviceFMS tcDeviceFMS = new TcDeviceFMS();
            tcDeviceFMS.transformTcPosMasterToSlave(tcDevice);

            try {
                String json = new ObjectMapper().writeValueAsString(tcDeviceFMS);
                log.info("TcPositionService.maintainReadModel - tc_devices : {}", json);
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }

            if (Envelope.Operation.DELETE.name().equals(operation.name())) {
                tcDeviceFMSRepo.deleteById(tcDevice.getId());
            } else {
                tcDeviceFMSRepo.save(tcDeviceFMS);
            } 
        } else if ("tc_geofences".equals(table)) {
            final TcGeofence tcGeofence = mapper.convertValue(traccarData, TcGeofence.class);
            log.info("TcPositionService.maintainReadModel - tc_geofences : {}", operation);
    
            TcGeofenceFMS tcGeofenceFMS = new TcGeofenceFMS();
            tcGeofenceFMS.transformTcPosMasterToSlave(tcGeofence);
    
            try {
                String json = new ObjectMapper().writeValueAsString(tcGeofenceFMS);
                log.info("TcPositionService.maintainReadModel - tc_geofences : {}", json);
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
    
            if (Envelope.Operation.DELETE.name().equals(operation.name())) {
                tcGeofenceFMSRepo.deleteById(tcGeofence.getId());
            } else {
                tcGeofenceFMSRepo.save(tcGeofenceFMS);
            } 
        }
    }

}
