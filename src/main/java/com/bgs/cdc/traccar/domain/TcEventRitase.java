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
@Table(name = "tc_events_ritase")
public class TcEventRitase {
    @Id
    private Long id;
    private String type;
    private Date eventtime;
    private Integer deviceid;
    private Long positionid;
    private Integer geofenceid;

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
