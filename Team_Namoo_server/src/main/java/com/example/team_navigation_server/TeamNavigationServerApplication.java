package com.example.team_navigation_server;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class TeamNavigationServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(TeamNavigationServerApplication.class, args);
    }

}
