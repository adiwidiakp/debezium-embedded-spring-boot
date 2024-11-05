package com.cdc.student.service;

import com.cdc.student.model.TcPosition;
import com.cdc.student.model.TcPositionFMS;
import com.cdc.student.repository.TcPositionFMSRepo;
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

    public void maintainReadModel(Map<String, Object> studentData, Envelope.Operation operation) {
        final ObjectMapper mapper = new ObjectMapper();
        final TcPosition tcPosition = mapper.convertValue(studentData, TcPosition.class);
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
