package com.example.team_navigation_server.member;


public class SignupRequest {

    private String loginId;

    private String password;

    private String email;

    private String nickname;

    public SignupRequest(String loginId, String password, String email, String nickname) {
        this.loginId = loginId;
        this.password = password;
        this.email = email;
        this.nickname = nickname;
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
