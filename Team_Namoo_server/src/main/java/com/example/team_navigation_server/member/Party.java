package com.example.team_navigation_server.member;

import jakarta.persistence.*;

@Entity
@Table(name = "parties")
public class Party {
    @Id

    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String name;

    private boolean isActive;

    public Party(String name, boolean isActive){
        this.name = name;
        this.isActive = isActive;
    }
    public Party(){}
    public Long getId(){return id;}
    public String getName(){return name;}
    public boolean isActive(){return isActive;}
}
