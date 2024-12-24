package com.bgs.cdc.traccar;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
//import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class App {

  //public static ConfigurableApplicationContext ctx;

  public static void main(String[] args) {
    //App.ctx = SpringApplication.run(App.class, args);
    SpringApplication.run(App.class, args);
  }

}
