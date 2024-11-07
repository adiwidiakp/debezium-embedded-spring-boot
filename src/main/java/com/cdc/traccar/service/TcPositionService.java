package com.cdc.traccar.service;

import com.cdc.traccar.model.TcPosition;
import com.cdc.traccar.model.TcPositionFMS;
import com.cdc.traccar.repository.TcPositionFMSRepo;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.debezium.data.Envelope;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;

@Slf4j
@Service
public class TcPositionService {

    @Autowired
    private TcPositionFMSRepo tcPositionFMSRepo;

    public void maintainReadModel(Map<String, Object> tcpPositionData, Envelope.Operation operation) {
        final ObjectMapper mapper = new ObjectMapper();
        final TcPosition tcPosition = mapper.convertValue(tcpPositionData, TcPosition.class);
        log.info("TcPositionService.maintainReadModel : {}", operation);

        TcPositionFMS tcPositionFMS = new TcPositionFMS();
        tcPositionFMS.transformTcPosMasterToSlave(tcPosition);

        try {
            String json = new ObjectMapper().writeValueAsString(tcPositionFMS);
            log.info("TcPositionService.maintainReadModel : {}", json);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        if (Envelope.Operation.DELETE.name().equals(operation.name())) {
            tcPositionFMSRepo.deleteById(tcPosition.getId());
        } else {
            tcPositionFMSRepo.save(tcPositionFMS);
        }
    }

}
