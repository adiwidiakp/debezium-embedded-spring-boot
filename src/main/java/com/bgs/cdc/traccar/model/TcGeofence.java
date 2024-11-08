package com.bgs.cdc.traccar.model;

import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.Id;

import com.fasterxml.jackson.annotation.JsonProperty;

@Data
@Entity
public class TcGeofence {

    @Id
    private Integer id;
    private String name;
    private String description;
    private String area;
    private String attributes;
    private Integer calendarid;
    private String geotype;
    @JsonProperty("group_name")
    private String groupName;
}
