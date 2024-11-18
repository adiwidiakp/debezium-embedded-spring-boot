package com.bgs.cdc.traccar.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.debezium.engine.RecordChangeEvent;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.kafka.connect.data.Field;
import org.apache.kafka.connect.data.Struct;
import org.apache.kafka.connect.source.SourceRecord;

import java.util.Map;

import static java.util.stream.Collectors.toMap;

@Slf4j
public class DebeziumUtils {
    public static Map<String, Object> getPayload(RecordChangeEvent<SourceRecord> sourceRecordRecordChangeEvent, String operation){
        var sourceRecord = sourceRecordRecordChangeEvent.record();
        //log.debug("Key = {}, Value = {}", sourceRecord.key(), sourceRecord.value());
        var sourceRecordChangeValue= (Struct) sourceRecord.value();
        Struct struct = (Struct) sourceRecordChangeValue.get(operation);
        if(struct==null){
            return null;
        }
        return struct.schema().fields().stream()
                .map(Field::name)
                .filter(fieldName -> struct.get(fieldName) != null)
                .map(fieldName -> Pair.of(fieldName, struct.get(fieldName)))
                .collect(toMap(Pair::getKey, Pair::getValue));
    }

    private final static ObjectMapper mapper = new ObjectMapper();

    public static <T> String toJson(T pojo) {
        String result;
        try {
            result = mapper.writeValueAsString(pojo);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return null;
        }

        return result;
    }

    public static <T> T fromJson(String json, Class<T> pojoClass) {
        T object;
        try {
            object = mapper.readValue(json, pojoClass);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return null;
        }

        return object;
    }

    public static <T> T fromJson(String json, TypeReference<T> valueTypeRef) {
        T object;
        try {
            object = mapper.<T>readValue(json, valueTypeRef);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            //-- PJMFJ = POJO JSON MAPPER FROM JSON
            return null;
        }

        return object;
    }

}
