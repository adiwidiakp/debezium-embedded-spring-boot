package com.bgs.cdc.traccar.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.Id;
import java.util.Date;

@Data
@Entity
public class TcDevice {

    @Id
    private Integer id;
    private String name;
    private String uniqueid;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
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
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date expirationtime;
    private boolean motionstate;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date motiontime;
    private double motiondistance;
    private boolean overspeedstate;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date overspeedtime;
    private Integer overspeedgeofenceid;
    private boolean motionstreak;
    private Integer calendarid;
    private Integer old_id;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUniqueid() {
        return uniqueid;
    }

    public void setUniqueid(String uniqueid) {
        this.uniqueid = uniqueid;
    }

    public Date getLastupdate() {
        return lastupdate;
    }

    public void setLastupdate(Date lastupdate) {
        this.lastupdate = lastupdate;
    }

    public Integer getPositionid() {
        return positionid;
    }

    public void setPositionid(Integer positionid) {
        this.positionid = positionid;
    }

    public Integer getGroupid() {
        return groupid;
    }

    public void setGroupid(Integer groupid) {
        this.groupid = groupid;
    }

    public String getAttributes() {
        return attributes;
    }

    public void setAttributes(String attributes) {
        this.attributes = attributes;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public String getContact() {
        return contact;
    }

    public void setContact(String contact) {
        this.contact = contact;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public boolean isDisabled() {
        return disabled;
    }

    public void setDisabled(boolean disabled) {
        this.disabled = disabled;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Date getExpirationtime() {
        return expirationtime;
    }

    public void setExpirationtime(Date expirationtime) {
        this.expirationtime = expirationtime;
    }

    public boolean isMotionstate() {
        return motionstate;
    }

    public void setMotionstate(boolean motionstate) {
        this.motionstate = motionstate;
    }

    public Date getMotiontime() {
        return motiontime;
    }

    public void setMotiontime(Date motiontime) {
        this.motiontime = motiontime;
    }

    public double getMotiondistance() {
        return motiondistance;
    }

    public void setMotiondistance(double motiondistance) {
        this.motiondistance = motiondistance;
    }

    public boolean isOverspeedstate() {
        return overspeedstate;
    }

    public void setOverspeedstate(boolean overspeedstate) {
        this.overspeedstate = overspeedstate;
    }

    public Date getOverspeedtime() {
        return overspeedtime;
    }

    public void setOverspeedtime(Date overspeedtime) {
        this.overspeedtime = overspeedtime;
    }

    public Integer getOverspeedgeofenceid() {
        return overspeedgeofenceid;
    }

    public void setOverspeedgeofenceid(Integer overspeedgeofenceid) {
        this.overspeedgeofenceid = overspeedgeofenceid;
    }

    public boolean isMotionstreak() {
        return motionstreak;
    }

    public void setMotionstreak(boolean motionstreak) {
        this.motionstreak = motionstreak;
    }

    public Integer getCalendarid() {
        return calendarid;
    }

    public void setCalendarid(Integer calendarid) {
        this.calendarid = calendarid;
    }

    public Integer getOld_id() {
        return old_id;
    }

    public void setOld_id(Integer old_id) {
        this.old_id = old_id;
    }
}
