package com.example.team_navigation_server.member;

public class MemberResponse {
    private Long id;
    private String loginId;
    private String email;
    private String nickname;
    private MemberRole role;

    public MemberResponse(Long id, String loginId, String email, String nickname, MemberRole role){
        this.id = id;
        this.loginId = loginId;
        this.email = email;
        this.nickname = nickname;
        this.role = role;
    }
    public Long getId(){
        return id;
    }
    public String getLoginId(){
        return loginId;
    }
    public String getEmail(){
        return email;
    }
    public String getNickname(){
        return nickname;
    }
    public MemberRole getRole() {
        return role;
    }
}
