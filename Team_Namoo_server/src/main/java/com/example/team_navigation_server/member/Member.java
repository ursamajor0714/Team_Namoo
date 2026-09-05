package com.example.team_navigation_server.member;

import jakarta.persistence.*;

import java.time.LocalDateTime;

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

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MemberRole role = MemberRole.USER;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MemberStatus status = MemberStatus.ACTIVE;

    @Column(nullable = false)
    private boolean emailVerified = false;

    // 회원가입 폼은 이미 수집하지만 아직 전송은 안 함(ADMIN_CONSOLE_BACKEND_TODO 1-1) - 전부 nullable.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "supported_party_id")
    private Party supportedParty;

    private String signupChannel;
    private String zipcode;
    private String addressBase;
    private String addressDetail;

    @Column(nullable = false)
    private boolean isPlus = false;

    @Column(nullable = false)
    private boolean agreeMarketing = false;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    private LocalDateTime lastAccessAt;

    protected Member() {
    }

    public Member(String loginId, String password, String email, String nickname) {
        this.loginId = loginId;
        this.password = password;
        this.email = email;
        this.nickname = nickname;
    }

    @PrePersist
    void onCreate() {
        this.createdAt = LocalDateTime.now();
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
    public MemberRole getRole() {
        return role;
    }
    public void setRole(MemberRole role) {
        this.role = role;
    }
    public MemberStatus getStatus() {
        return status;
    }
    public void setStatus(MemberStatus status) {
        this.status = status;
    }
    public boolean isEmailVerified() {
        return emailVerified;
    }
    public void setEmailVerified(boolean emailVerified) {
        this.emailVerified = emailVerified;
    }
    public Party getSupportedParty() {
        return supportedParty;
    }
    public void setSupportedParty(Party supportedParty) {
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
    public boolean isPlus() {
        return isPlus;
    }
    public void setPlus(boolean plus) {
        isPlus = plus;
    }
    public boolean isAgreeMarketing() {
        return agreeMarketing;
    }
    public void setAgreeMarketing(boolean agreeMarketing) {
        this.agreeMarketing = agreeMarketing;
    }
    public void setNickname(String nickname) {
        this.nickname = nickname;
    }
    public void setEmail(String email) {
        this.email = email;
    }
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    public LocalDateTime getLastAccessAt() {
        return lastAccessAt;
    }
    public void setLastAccessAt(LocalDateTime lastAccessAt) {
        this.lastAccessAt = lastAccessAt;
    }
}
