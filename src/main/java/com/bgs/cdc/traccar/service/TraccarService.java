package com.bgs.cdc.traccar.service;

import com.bgs.cdc.traccar.domain.TcDevice;
import com.bgs.cdc.traccar.domain.TcEvent;
import com.bgs.cdc.traccar.domain.EventRitase;
import com.bgs.cdc.traccar.domain.TcGeofence;
import com.bgs.cdc.traccar.domain.TcPosition;
import com.bgs.cdc.traccar.repository.DeviceRepository;
import com.bgs.cdc.traccar.repository.EventRepository;
import com.bgs.cdc.traccar.repository.EventRitaseRepository;
import com.bgs.cdc.traccar.repository.GeofenceRepository;
import com.bgs.cdc.traccar.repository.PositionRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.debezium.data.Envelope.Operation;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class TraccarService {

    private final PositionRepository positionRepository;
    private final EventRepository eventsRepository;
    private final EventRitaseRepository eventRitaseRepository;
    private final DeviceRepository devicesRepository;
    private final GeofenceRepository geofencesRepository;

    private static final String tc_positions = "tc_positions";
    private static final String tc_events = "tc_events";
    private static final String tc_devices = "tc_devices";
    private static final String tc_geofences = "tc_geofences";
    private static final String geofenceExit = "geofenceExit";
    private static final String geofenceEnter = "geofenceEnter";

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
                String eventType = event.getType();
                boolean isInsert = false;
                String geoName = null;
                String geoAttributes = null;
                if (geofenceEnter.equals(eventType)) {
                    Optional<TcGeofence> geofence = getGeofence(event.getGeofenceid());

                    if (geofence.isPresent()) {
                        geoName = geofence.get().getName();
                        geoAttributes = geofence.get().getAttributes();
                        
                        if ((geoName.contains("KM") && geoAttributes.contains("KM Hauling")) || geoAttributes.contains("ROM") || (geoName.contains("PORT") && geoAttributes.contains("Antrian"))) {   
                            isInsert = true;
                        }
                    }
                } else if (geofenceExit.equals(eventType)) {
                    Optional<TcGeofence> geofence = getGeofence(event.getGeofenceid());

                    if (geofence.isPresent()) {
                        geoName = geofence.get().getName();
                        geoAttributes = geofence.get().getAttributes();                    
                        if (geoAttributes.contains("ROM") || (geoAttributes.contains("PORT") && !"PORT BIB".equals(geoName) && !geoAttributes.contains("PORT BIB"))) {
                            isInsert = true;
                        }
                    }
                }
                if (isInsert) {
                    Optional<TcDevice> device = getDevice(event.getDeviceid());

                    if (device.isPresent()) {
                        EventRitase eventRitase = new EventRitase();
                        eventRitase.setType(eventType);
                        eventRitase.setId(event.getId());
                        eventRitase.setEventtime(event.getEventtime());
                        eventRitase.setDeviceid(event.getDeviceid());
                        eventRitase.setPositionid(event.getPositionid());
                        eventRitase.setGeofenceid(event.getGeofenceid());
                        eventRitase.setGeoname(geoName);
                        eventRitase.setGeoattributes(geoAttributes);
                        eventRitase.setDevname(device.get().getName());
                        eventRitaseRepository.save(eventRitase);
                    }
                }
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
    @Cacheable(value = "geofence", key = "#geofenceid")
    private Optional<TcGeofence> getGeofence(Long geofenceid) {
        List<Object[]> result = geofencesRepository.findByGeofenceId(geofenceid);
        if (result.isEmpty()) {
            return Optional.empty();
        } else {
            TcGeofence geofence = new TcGeofence();
            geofence.setId(Long.parseLong(result.get(0)[0].toString()));
            geofence.setName(result.get(0)[1].toString());
            geofence.setAttributes(result.get(0)[2].toString());
            return Optional.of(geofence);
        }
    }
    @Cacheable(value = "device", key = "#deviceid")
    private Optional<TcDevice> getDevice(Long deviceid) {
        List<Object[]> result = devicesRepository.findByDeviceId(deviceid);
        if (result.isEmpty()) {
            return Optional.empty();
        } else {
            TcDevice device = new TcDevice();
            device.setId(Long.parseLong(result.get(0)[0].toString()));
            device.setName(result.get(0)[1].toString());
            return Optional.of(device);
        }
    }
}