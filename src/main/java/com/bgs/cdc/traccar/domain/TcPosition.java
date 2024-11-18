package com.bgs.cdc.traccar.domain;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.TemporalType;
import javax.persistence.Temporal;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.Data;

@Data
@Entity
@Table(name = "tc_positions")
public class TcPosition {

    @Id
    private Long id;
    private String protocol;
    private int deviceid;
    @JsonFormat(pattern="yyyy-MM-dd HH:mm:ss")
    private Date servertime;
    //@JsonFormat(pattern="yyyy-MM-dd HH:mm:ss")
    @Temporal(TemporalType.TIMESTAMP)
    private Date devicetime;
    //@JsonFormat(pattern="yyyy-MM-dd HH:mm:ss")
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
}
