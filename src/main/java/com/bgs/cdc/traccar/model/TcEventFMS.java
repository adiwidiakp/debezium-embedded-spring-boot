package com.bgs.cdc.traccar.model;

import lombok.Data;

import javax.persistence.*;
import java.util.Date;

@Entity
@Data
@Table(name = "tc_events")
public class TcEventFMS {
    
    @Id
    private Integer id;
    private String type;
    @Temporal(TemporalType.TIMESTAMP)
    private Date eventtime;
    private int deviceid;
    private int positionid;
    private int geofenceid;
    private String attributes;
    private int maintenanceid;

    public TcEventFMS(int id_, String type_, java.sql.Date eventtime_, int deviceid_, int positionid_, int geofenceid_, String attributes_, int maintenanceid_) {
        this.id = id_;
        this.type = type_;
        this.eventtime = eventtime_;
        this.deviceid = deviceid_;
        this.positionid = positionid_;
        this.geofenceid = geofenceid_;
        this.attributes = attributes_;
        this.maintenanceid = maintenanceid_;
    }

    public TcEventFMS() {

    }

    public void transformTcPosMasterToSlave(TcEvent tcEvent) {
        this.id = tcEvent.getId();
        this.type = tcEvent.getType();
        this.eventtime = tcEvent.getEventtime();
        this.deviceid = tcEvent.getDeviceid();
        this.positionid = tcEvent.getPositionid();
        this.geofenceid = tcEvent.getGeofenceid();
        this.attributes = tcEvent.getAttributes();
        this.maintenanceid = tcEvent.getMaintenanceid();
    }

}
