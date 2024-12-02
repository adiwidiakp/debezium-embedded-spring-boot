package com.bgs.cdc.traccar.domain;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.TemporalType;
import javax.persistence.Temporal;

import lombok.Data;

@Data
@Entity
@Table(name = "tc_positions")
public class TcPosition {

    @Id
    private Long id;
    private String protocol;
    private Integer deviceid;
    private Date servertime;
    @Temporal(TemporalType.TIMESTAMP)
    private Date devicetime;
    @Temporal(TemporalType.TIMESTAMP)
    private Date fixtime;
    private boolean valid;
    private Double latitude;
    private Double longitude;
    private Float altitude;
    private Float speed;
    private Float course;
    private String address;
    private String attributes;
    private double accuracy;
    private String network;
    private String geofenceids;

    public void setServertime(Date servertime) {
        Instant instant = servertime.toInstant();
        ZonedDateTime utcDateTime = instant.atZone(ZoneId.of("UTC"));
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        String formattedDate = utcDateTime.format(formatter);
        LocalDateTime localDateTime = LocalDateTime.parse(formattedDate, formatter);
        ZoneId zoneId = ZoneId.systemDefault();
        ZonedDateTime zonedDateTime = localDateTime.atZone(zoneId);

        this.servertime = Date.from(zonedDateTime.toInstant());
    }

}
