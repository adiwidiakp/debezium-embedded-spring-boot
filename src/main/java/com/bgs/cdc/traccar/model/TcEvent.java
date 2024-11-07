package com.bgs.cdc.traccar.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;


import javax.persistence.Entity;
import javax.persistence.Id;
import java.util.Date;

@Data
@Entity
public class TcEvent {

    @Id
    private Integer id;
    private String type;
    @JsonFormat(pattern="yyyy-MM-dd HH:mm:ss")
    private Date eventtime;
    private int deviceid;
    private int positionid;
    private int geofenceid;
    private String attributes;
    private int maintenanceid;

}