package com.jyrp.team_navigation_server;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class TeamNavigationServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(TeamNavigationServerApplication.class, args);
    }

}
