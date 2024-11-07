package com.cdc.traccar.service;

import com.cdc.traccar.model.TcEvents;
import com.cdc.traccar.repository.TcEventsRepo;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.debezium.data.Envelope;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;

@Slf4j
@Service
public class TcEventsService {

    @Autowired
    private TcEventsRepo tcEventsRepo;

    public void maintainReadModel(Map<String, Object> tcpPositionData, Envelope.Operation operation) {
        final ObjectMapper mapper = new ObjectMapper();
        final TcEvents tcEvents = mapper.convertValue(tcpPositionData, TcEvents.class);
        log.info("tcEventsService.maintainReadModel : {}", operation);

        try {
            String json = new ObjectMapper().writeValueAsString(tcEvents);
            log.info("tcEventsService.maintainReadModel : {}", json);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        if (Envelope.Operation.DELETE.name().equals(operation.name())) {
            tcEventsRepo.deleteById(tcEvents.getId());
        } else {
            tcEventsRepo.save(tcEvents);
        }
    }

}
