package com.example.team_navigation_server.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

@Configuration
public class S3Config {

    @Value("${aws.s3.region}")
    private String region;

    // 자격증명은 기본 체인(로컬: 환경변수/~/.aws, EC2: 인스턴스 롤)을 그대로 쓴다 - 코드에 키를 넣지 않는다.
    @Bean
    public S3Presigner s3Presigner() {
        return S3Presigner.builder()
                .region(Region.of(region))
                .build();
    }
}
