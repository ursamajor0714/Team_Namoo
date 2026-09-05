package com.example.team_navigation_server.admin;

import com.example.team_navigation_server.member.MemberStatus;
import jakarta.validation.constraints.NotNull;

public class AdminMemberStatusRequest {
    @NotNull
    private MemberStatus status;

    public MemberStatus getStatus() { return status; }
    public void setStatus(MemberStatus status) { this.status = status; }
}
