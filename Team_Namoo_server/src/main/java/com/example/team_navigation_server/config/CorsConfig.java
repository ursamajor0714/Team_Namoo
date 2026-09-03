package com.example.team_navigation_server.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * 프론트에서 모든 API(로그인 포함)를 호출할 수 있도록 전역 CORS를 허용한다.
 * 이전에는 NewsController에만 @CrossOrigin이 붙어있어서 로그인 등 다른 컨트롤러는
 * CORS가 전혀 허용되지 않았다.
 * 로그인이 세션 쿠키(JSESSIONID)를 쓰고 프론트가 withCredentials:true로 호출하므로,
 * allowedOrigins는 "*"를 쓸 수 없고 구체적인 origin을 명시해야 한다.
 * 허용 origin 목록은 app.cors.allowed-origins(쉼표 구분)로 재정의할 수 있고,
 * 기본값은 로컬 Vite 개발 서버 + 배포된 Amplify 프론트다.
 * 배포 시 환경변수 APP_CORS_ALLOWED_ORIGINS 로 주입한다.
 */
@Configuration
public class CorsConfig implements WebMvcConfigurer {

    @Value("${app.cors.allowed-origins:http://localhost:5173,https://main.d11ftaq8rgsma0.amplifyapp.com}")
    private String[] allowedOrigins;

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins(allowedOrigins)
                .allowedMethods("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true);
    }
}
