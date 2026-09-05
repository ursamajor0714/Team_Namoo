package com.example.team_navigation_server.admin;

import com.example.team_navigation_server.member.MemberRole;
import jakarta.validation.constraints.NotNull;

public class AdminMemberRoleRequest {
    @NotNull
    private MemberRole role;

    public MemberRole getRole() { return role; }
    public void setRole(MemberRole role) { this.role = role; }
}
