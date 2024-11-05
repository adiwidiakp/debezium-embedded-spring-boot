package com.cdc.student.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.Id;
import java.util.Date;

@Data
@Entity
public class TcPosition {

    @Id
    private Integer id;
    private String protocol;
    private int deviceid;
    @JsonFormat(pattern="yyyy-MM-dd HH:mm:ss")
    private Date servertime;
    @JsonFormat(pattern="yyyy-MM-dd HH:mm:ss")
    private Date devicetime;
    @JsonFormat(pattern="yyyy-MM-dd HH:mm:ss")
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
    private Integer old_id;
}
