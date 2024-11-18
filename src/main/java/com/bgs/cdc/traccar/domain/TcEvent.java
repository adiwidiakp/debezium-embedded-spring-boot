package com.bgs.cdc.traccar.domain;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.Data;

@Data
@Entity
@Table(name = "tc_events")
public class TcEvent {
    @Id
    private Long id;
    private String type;
    @JsonFormat(pattern="yyyy-MM-dd HH:mm:ss")
    private Date eventtime;
    private int deviceid;
    private int positionid;
    private int geofenceid;
    private String attributes;
    private int maintenanceid;
}
