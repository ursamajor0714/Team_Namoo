package com.example.team_navigation_server.member;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class SignupRequest {
    @NotBlank(message ="아이디를 입력해주세요." )
    @Pattern(regexp = "^[A-Za-z0-9]{4,20}$",message = "영문/숫자만 입력 가능합니다.")
    private String loginId;

    @NotBlank(message = "비밀번호를 입력해주세요.")
    @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[^A-Za-z0-9]).{8,}$",
    message = "비밀번호는 대소문자, 숫자, 특수문자를 포함해 8자 이상이어야 합니다")
    private String password;

    @NotBlank(message = "이메일을 입력해주세요.")
    @Size(max = 100)
    @Email
    private String email;

    @NotBlank(message = "닉네임을 입력해주세요.")
    @Size(min = 2, max = 12,message = "2~12자까지 입력 가능합니다.")
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
