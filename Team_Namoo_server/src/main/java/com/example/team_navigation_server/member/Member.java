package com.example.team_navigation_server.member;

import jakarta.persistence.*;

@Entity
@Table(name = "members")
public class Member {
    @Id

    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String loginId;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String nickname;

    protected Member() {
    }

    public Member(String loginId, String password, String email, String nickname) {
        this.loginId = loginId;
        this.password = password;
        this.email = email;
        this.nickname = nickname;
    }
    public Long getId() {
        return id;
    }
    public String getLoginId() {
        return loginId;
    }
    public String getPassword(){
        return password;
    }
    public String getEmail(){
        return email;
    }
    public String getNickname(){
        return nickname;
    }

}
