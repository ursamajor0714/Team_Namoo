package com.example.team_navigation_server.oauth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

// SNS 최초 로그인 시 이메일/가입경로 외에 추가로 받아야 하는 최소 프로필 - SignupRequest와 동일한
// 닉네임/지지정당 규칙을 따른다 (loginId/password/email은 이미 확보돼 있어 여기서 안 받음).
public class OAuthSignupCompleteRequest {

    @NotBlank(message = "닉네임을 입력해주세요.")
    @Size(min = 2, max = 12, message = "2~12자까지 입력 가능합니다.")
    private String nickname;

    @NotBlank(message = "지지 정당을 선택해주세요.")
    private String supportedParty;

    private String signupChannel;
    private String zipcode;
    private String addressBase;
    private String addressDetail;
    private boolean agreeMarketing;

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getSupportedParty() {
        return supportedParty;
    }

    public void setSupportedParty(String supportedParty) {
        this.supportedParty = supportedParty;
    }

    public String getSignupChannel() {
        return signupChannel;
    }

    public void setSignupChannel(String signupChannel) {
        this.signupChannel = signupChannel;
    }

    public String getZipcode() {
        return zipcode;
    }

    public void setZipcode(String zipcode) {
        this.zipcode = zipcode;
    }

    public String getAddressBase() {
        return addressBase;
    }

    public void setAddressBase(String addressBase) {
        this.addressBase = addressBase;
    }

    public String getAddressDetail() {
        return addressDetail;
    }

    public void setAddressDetail(String addressDetail) {
        this.addressDetail = addressDetail;
    }

    public boolean isAgreeMarketing() {
        return agreeMarketing;
    }

    public void setAgreeMarketing(boolean agreeMarketing) {
        this.agreeMarketing = agreeMarketing;
    }
}
