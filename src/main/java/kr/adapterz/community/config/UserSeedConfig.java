package kr.adapterz.community.config;

import kr.adapterz.community.service.UserSeedService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

// @Profile({"dev", "prod"})
@Configuration
@Profile("dev")
@RequiredArgsConstructor
public class UserSeedConfig {

    private final UserSeedService userSeedService;

    @Bean
    ApplicationRunner userSeedRunner() {
        return args -> userSeedService.userSeed();
    }
}
