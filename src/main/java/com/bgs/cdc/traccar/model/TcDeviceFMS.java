package com.bgs.cdc.traccar.model;

import lombok.Data;

import javax.persistence.*;
import java.util.Date;

@Entity
@Data
@Table(name = "tc_devices")
public class TcDeviceFMS {

    @Id
    private Integer id;
    private String name;
    private String uniqueid;
    @Temporal(TemporalType.TIMESTAMP)
    private Date lastupdate;
    private Integer positionid;
    private Integer groupid;
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
    private Integer overspeedgeofenceid;
    private boolean motionstreak;
    private Integer calendarid;

    public TcDeviceFMS(Integer id_, String name_, String uniqueid_, java.sql.Timestamp lastupdate_, Integer positionid_, Integer groupid_, String attributes_, String phone_, String model_, String contact_, String category_, boolean disabled_, String status_, String geofenceids_, java.sql.Timestamp expirationtime_, boolean motionstate_, java.sql.Timestamp motiontime_, double motiondistance_, boolean overspeedstate_, java.sql.Timestamp overspeedtime_, Integer overspeedgeofenceid_, boolean motionstreak_, Integer calendarid_) {
        this.id = id_;
        this.name = name_;
        this.uniqueid = uniqueid_;
        this.lastupdate = lastupdate_;
        this.positionid = positionid_;
        this.groupid = groupid_;
        this.attributes = attributes_;
        this.phone = phone_;
        this.model = model_;
        this.contact = contact_;
        this.category = category_;
        this.disabled = disabled_;
        this.status = status_;
        this.geofenceids = geofenceids_;
        this.expirationtime = expirationtime_;
        this.motionstate = motionstate_;
        this.motiontime = motiontime_;
        this.motiondistance = motiondistance_;
        this.overspeedstate = overspeedstate_;
        this.overspeedtime = overspeedtime_;
        this.overspeedgeofenceid = overspeedgeofenceid_;
        this.motionstreak = motionstreak_;
        this.calendarid = calendarid_;
    }

    public TcDeviceFMS() {

    }

    public void transformTcPosMasterToSlave(TcDevice tcDevice) {

        this.id = tcDevice.getId();
        this.name = tcDevice.getName();
        this.uniqueid = tcDevice.getUniqueid();
        this.lastupdate = tcDevice.getLastupdate();
        this.positionid = tcDevice.getPositionid();
        this.groupid = tcDevice.getGroupid();
        this.attributes = tcDevice.getAttributes();
        this.phone = tcDevice.getPhone();
        this.model = tcDevice.getModel();
        this.contact = tcDevice.getContact();
        this.category = tcDevice.getCategory();
        this.disabled = tcDevice.isDisabled();
        this.status = tcDevice.getStatus();
        this.expirationtime = tcDevice.getExpirationtime();
        this.motionstate = tcDevice.isMotionstate();
        this.motiontime = tcDevice.getMotiontime();
        this.motiondistance = tcDevice.getMotiondistance();
        this.overspeedstate = tcDevice.isOverspeedstate();
        this.overspeedtime = tcDevice.getOverspeedtime();
        this.overspeedgeofenceid = tcDevice.getOverspeedgeofenceid();
        this.motionstreak = tcDevice.isMotionstreak();
        this.calendarid = tcDevice.getCalendarid();

    }

}
