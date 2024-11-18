package com.bgs.cdc.traccar.domain;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
@Entity
@Table(name = "tc_geofences")
public class TcGeofence {
    @Id
    private Long id;
    private String name;
    private String description;
    private String area;
    private String attributes;
    private Integer calendarid;
    private String geotype;
    @JsonProperty("group_name")
    private String groupName;
}