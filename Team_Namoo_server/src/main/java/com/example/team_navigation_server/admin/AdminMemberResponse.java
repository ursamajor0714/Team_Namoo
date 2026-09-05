package com.example.team_navigation_server.admin;

import com.example.team_navigation_server.member.Member;
import com.example.team_navigation_server.member.MemberRole;
import com.example.team_navigation_server.member.MemberStatus;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;

public class AdminMemberResponse {
    private final Long id;
    private final String loginId;
    private final String nickname;
    private final String email;
    private final boolean emailVerified;
    private final String supportedParty;
    private final String signupChannel;
    private final String zipcode;
    private final String addressBase;
    private final String addressDetail;
    private final LocalDateTime joinedAt;
    private final LocalDateTime lastAccessAt;
    private final MemberStatus status;
    private final boolean isPlus;
    private final boolean agreeMarketing;
    private final MemberRole role;

    public AdminMemberResponse(Member member) {
        this.id = member.getId();
        this.loginId = member.getLoginId();
        this.nickname = member.getNickname();
        this.email = member.getEmail();
        this.emailVerified = member.isEmailVerified();
        this.supportedParty = member.getSupportedParty() == null ? null : member.getSupportedParty().getName();
        this.signupChannel = member.getSignupChannel();
        this.zipcode = member.getZipcode();
        this.addressBase = member.getAddressBase();
        this.addressDetail = member.getAddressDetail();
        this.joinedAt = member.getCreatedAt();
        this.lastAccessAt = member.getLastAccessAt();
        this.status = member.getStatus();
        this.isPlus = member.isPlus();
        this.agreeMarketing = member.isAgreeMarketing();
        this.role = member.getRole();
    }

    public Long getId() { return id; }
    public String getLoginId() { return loginId; }
    public String getNickname() { return nickname; }
    public String getEmail() { return email; }
    public boolean isEmailVerified() { return emailVerified; }
    public String getSupportedParty() { return supportedParty; }
    public String getSignupChannel() { return signupChannel; }
    public String getZipcode() { return zipcode; }
    public String getAddressBase() { return addressBase; }
    public String getAddressDetail() { return addressDetail; }
    public LocalDateTime getJoinedAt() { return joinedAt; }
    public LocalDateTime getLastAccessAt() { return lastAccessAt; }
    public MemberStatus getStatus() { return status; }
    @JsonProperty("isPlus")
    public boolean isPlus() { return isPlus; }
    public boolean isAgreeMarketing() { return agreeMarketing; }
    public MemberRole getRole() { return role; }
}
