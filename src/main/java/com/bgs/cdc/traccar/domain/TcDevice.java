package com.bgs.cdc.traccar.domain;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import lombok.Data;

@Data
@Entity
@Table(name = "tc_devices")
public class TcDevice {
    @Id
    private Long id;
    private String name;
    private String uniqueid;
    @Temporal(TemporalType.TIMESTAMP)
    private Date lastupdate;
    private Long positionid;
    private Long groupid;
    private String attributes;
    private String phone;
    private String model;
    private String contact;
    private String category;
    private boolean disabled;
    private String status;
    private String geofenceids;
    @Temporal(TemporalType.TIMESTAMP)
    private Date expirationtime;
    private boolean motionstate;
    @Temporal(TemporalType.TIMESTAMP)
    private Date motiontime;
    private double motiondistance;
    private boolean overspeedstate;
    @Temporal(TemporalType.TIMESTAMP)
    private Date overspeedtime;
    private Long overspeedgeofenceid;
    private boolean motionstreak;
    private Long calendarid;
}
