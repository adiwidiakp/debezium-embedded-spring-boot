package com.bgs.cdc.traccar.domain;

/*import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;*/
import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.Data;

@Data
@Entity
@Table(name = "events_ritase")
public class EventRitase {
    @Id
    private Long id;
    private Date eventtime;
    private Long deviceid;
    private Long positionid;
    private Long geofenceid;
    private String type;
    private String geoname;
    private String geoattributes;
    private String devname;

/*   public void setEventtime(Date eventtime) {
        Instant instant = eventtime.toInstant();
        ZonedDateTime utcDateTime = instant.atZone(ZoneId.of("UTC"));
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        
        String formattedDate = utcDateTime.format(formatter);
        LocalDateTime localDateTime = LocalDateTime.parse(formattedDate, formatter);
        ZoneId zoneId = ZoneId.systemDefault(); 
        ZonedDateTime zonedDateTime = localDateTime.atZone(zoneId);

        this.eventtime = Date.from(zonedDateTime.toInstant());
    }
        */
}
