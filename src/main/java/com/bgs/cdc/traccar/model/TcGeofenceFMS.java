package com.bgs.cdc.traccar.model;

import lombok.Data;

import javax.persistence.*;

@Entity
@Data
@Table(name = "tc_geofences")
public class TcGeofenceFMS {

    @Id
    private Integer id;
    private String name;
    private String description;
    private String area;
    private String attributes;
    private Integer calendarid;
    private String geotype;
    private String groupName;

    public TcGeofenceFMS(Integer id_, String name_, String description_, String area_, String attributes_, Integer calendarid_, String geotype_, String groupName_) {
        this.id = id_;
        this.name = name_;
        this.description = description_;
        this.area = area_;
        this.attributes = attributes_;
        this.calendarid = calendarid_;
        this.geotype = geotype_;
        this.groupName = groupName_;
    }

    public TcGeofenceFMS() {
    }

    public void transformTcPosMasterToSlave(TcGeofence tcGeofence) {

        this.id = tcGeofence.getId();
        this.name = tcGeofence.getName();
        this.description = tcGeofence.getDescription();
        this.area = tcGeofence.getArea();
        this.attributes = tcGeofence.getAttributes();
        this.calendarid = tcGeofence.getCalendarid();
        this.geotype = tcGeofence.getGeotype();
        this.groupName =  tcGeofence.getGroupName();

    }

}
