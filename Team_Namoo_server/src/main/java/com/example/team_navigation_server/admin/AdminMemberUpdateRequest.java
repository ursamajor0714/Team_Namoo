package com.example.team_navigation_server.admin;

/** 상세 팝업 "저장". null 인 필드는 변경하지 않는다 (loginId/joinedAt/lastAccessAt/role/status 는 이 API로 못 바꿈). */
public class AdminMemberUpdateRequest {
    private String nickname;
    private String email;
    private Boolean emailVerified;
    private String supportedParty;
    private String signupChannel;
    private String zipcode;
    private String addressBase;
    private String addressDetail;
    private Boolean isPlus;
    private Boolean agreeMarketing;

    public String getNickname() { return nickname; }
    public void setNickname(String nickname) { this.nickname = nickname; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public Boolean getEmailVerified() { return emailVerified; }
    public void setEmailVerified(Boolean emailVerified) { this.emailVerified = emailVerified; }
    public String getSupportedParty() { return supportedParty; }
    public void setSupportedParty(String supportedParty) { this.supportedParty = supportedParty; }
    public String getSignupChannel() { return signupChannel; }
    public void setSignupChannel(String signupChannel) { this.signupChannel = signupChannel; }
    public String getZipcode() { return zipcode; }
    public void setZipcode(String zipcode) { this.zipcode = zipcode; }
    public String getAddressBase() { return addressBase; }
    public void setAddressBase(String addressBase) { this.addressBase = addressBase; }
    public String getAddressDetail() { return addressDetail; }
    public void setAddressDetail(String addressDetail) { this.addressDetail = addressDetail; }
    public Boolean getIsPlus() { return isPlus; }
    public void setIsPlus(Boolean isPlus) { this.isPlus = isPlus; }
    public Boolean getAgreeMarketing() { return agreeMarketing; }
    public void setAgreeMarketing(Boolean agreeMarketing) { this.agreeMarketing = agreeMarketing; }
}
