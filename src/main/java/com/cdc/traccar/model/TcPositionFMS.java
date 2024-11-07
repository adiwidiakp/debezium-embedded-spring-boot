package com.cdc.traccar.model;

import lombok.Data;

import javax.persistence.*;
import java.sql.Timestamp;
import java.util.Date;

@Entity
@Data
@Table(name = "tc_positions")
public class TcPositionFMS {

    @Id
    private Integer id;
    private String protocol;
    private int deviceid;
    private Date servertime;
    @Temporal(TemporalType.TIMESTAMP)
    private Date devicetime;
    @Temporal(TemporalType.TIMESTAMP)
    private Date fixtime;
    private boolean valid;
    private double latitude;
    private double longitude;
    private float altitude;
    private float speed;
    private float course;
    private String address;
    private String attributes;
    private double accuracy;
    private String network;
    private String geofenceids;

    public TcPositionFMS(int id_,String protocol_,int deviceid_,java.sql.Date servertime_,java.sql.Timestamp devicetime_,java.sql.Timestamp fixtime_,boolean valid_,double latitude_,double longitude_,float altitude_,float speed_,float course_,String address_,String attributes_,double accuracy_,String network_,String geofenceids_)
    {
        this.id = id_;
        this.protocol = protocol_;
        this.deviceid = deviceid_;
        this.servertime = servertime_;
        this.devicetime = devicetime_;
        this.fixtime = fixtime_;
        this.valid = valid_;
        this.latitude = latitude_;
        this.longitude = longitude_;
        this.altitude = altitude_;
        this.speed = speed_;
        this.course = course_;
        this.address = address_;
        this.attributes = attributes_;
        this.accuracy = accuracy_;
        this.network = network_;
        this.geofenceids = geofenceids_;
    }

    public TcPositionFMS() {

    }

    public void transformTcPosMasterToSlave(TcPosition tcPosition) {

        this.id = tcPosition.getId();
        this.protocol = tcPosition.getProtocol();
        this.deviceid = tcPosition.getDeviceid();
        this.servertime = tcPosition.getServertime();
        this.devicetime = tcPosition.getDevicetime();
        this.fixtime = tcPosition.getFixtime();
        this.valid = tcPosition.isValid();
        this.latitude = tcPosition.getLatitude();
        this.longitude = tcPosition.getLongitude();
        this.altitude = tcPosition.getAltitude();
        this.speed = tcPosition.getSpeed();
        this.course = tcPosition.getCourse();
        this.address = tcPosition.getAddress();
        this.attributes = tcPosition.getAttributes();
        this.accuracy = tcPosition.getAccuracy();
        this.network = tcPosition.getNetwork();
        this.geofenceids = tcPosition.getGeofenceids();

    }

}
