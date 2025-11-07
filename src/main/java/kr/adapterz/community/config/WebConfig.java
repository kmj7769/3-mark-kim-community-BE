package kr.adapterz.community.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

// 스프링 서버 전역적으로 CORS 설정
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Value("#{'${cors.allowed-origins}'.split(',')}")
    private String[] allowedOrigins;

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins(allowedOrigins) // 허용할 출처
                .allowedMethods("*") // 허용할 HTTP method
                .allowCredentials(true) // 쿠키 인증 요청 허용
                .maxAge(86400); // 원하는 시간만큼 pre-flight 리퀘스트를 캐싱
    }
}