package kr.adapterz.community.service;

import kr.adapterz.community.entity.User;
import kr.adapterz.community.entity.UserAuth;
import kr.adapterz.community.repository.UserAuthRepository;
import kr.adapterz.community.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.stream.IntStream;

@Component
@RequiredArgsConstructor
public class UserSeedService {
    private final UserRepository userRepository;
    private final UserAuthRepository userAuthRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public void userSeed() {
        if (userRepository.count() >= 5) return;

        IntStream.range(1, 6).forEach(i -> {
            String rawPassword = "testPassword" + i + "!";
            String encodedPassword = passwordEncoder.encode(rawPassword);

            User user = new User("user" + i, null);
            User savedUser = userRepository.saveAndFlush(user);

            UserAuth userAuth = new UserAuth(savedUser, "user" + i + "@example.com", encodedPassword);
            userAuthRepository.save(userAuth);
        });
    }
}
