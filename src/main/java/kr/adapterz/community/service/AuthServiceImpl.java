package kr.adapterz.community.service;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import kr.adapterz.community.dto.auth.LoginRequestDto;
import kr.adapterz.community.dto.auth.LoginResponseDto;
import kr.adapterz.community.entity.RefreshToken;
import kr.adapterz.community.entity.UserAuth;
import kr.adapterz.community.jwt.JwtProvider;
import kr.adapterz.community.repository.RefreshTokenRepository;
import kr.adapterz.community.repository.UserAuthRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserAuthRepository userAuthRepository;
    private final RefreshTokenRepository refreshTokenRepository;

    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;

    private static final int ACCESS_TTL = 15 * 60;
    private static final int REFRESH_TTL = 14 * 24 * 60 * 60;

    @Transactional
    public Optional<LoginResponseDto> login(LoginRequestDto loginRequestDto, HttpServletResponse response) {

        // 해당 email을 가지는 유저 인증 정보 찾기
        Optional<UserAuth> userAuthOpt = userAuthRepository.findByEmail(loginRequestDto.getEmail());

        // 해당 email을 가지는 유저 인증 정보가 없을 경우, 빈 Optional 반환
        if (userAuthOpt.isEmpty()) {
            return Optional.empty();
        }

        UserAuth userAuth = userAuthOpt.get();

        // 해당 email을 가지는 유저 인증 정보의 password와 일치하지 않을 경우, 빈 Optional 반환
        if (!passwordEncoder.matches(loginRequestDto.getPassword(), userAuth.getPassword())) {
            return Optional.empty();
        }

        // 기존 refresh 토큰 무효화
        refreshTokenRepository.deleteByUserId(userAuth.getUserId());

        // 새로운 토큰 발급 및 저장
        var tokenResponse = generateAndSaveTokens(userAuth);

        // 쿠키 추가
        addTokenCookies(response, tokenResponse);

        // 로그인 성공한 유저의 아이디와 닉네임을 DTO에 매핑
        return Optional.of(new LoginResponseDto(userAuth.getUserId(), userAuth.getUser().getNickname(), userAuth.getUser().getProfileImage()));
    }

    @Transactional
    public Boolean refreshTokens(String refreshToken, HttpServletResponse response) {
        var parsedRefreshToken = jwtProvider.parse(refreshToken);

        RefreshToken entity = refreshTokenRepository.findByTokenAndRevokedFalse(refreshToken).orElse(null);

        if (entity == null || entity.getExpiresAt().isBefore(Instant.now())) {
            return false;
        }

        Long userId = Long.valueOf(parsedRefreshToken.getBody().getSubject());
        UserAuth userAuth = userAuthRepository.findByUserId(userId).orElse(null);

        if (userAuth == null) {
            return false;
        }

        // 기존 refresh 토큰 무효화
        refreshTokenRepository.deleteByUserId(userAuth.getUserId());

        // refresh token도 access token과 함께 새로 발급
        var tokenResponse = generateAndSaveTokens(userAuth);

        // 쿠키 추가
        addTokenCookies(response, tokenResponse);

        return true;
    }

    @Transactional
    public void logout(HttpServletResponse response) {
        // Access / Refresh 토큰의 유효 수명을 0으로 초기화
        addTokenCookie(response, "accessToken", null, 0);
        addTokenCookie(response, "refreshToken", null, 0);
    }

    // Access / Refresh 토큰을 새로 발급하고 DB에 저장
    private TokenResponse generateAndSaveTokens(UserAuth userAuth) {
        String accessToken = jwtProvider.createAccessToken(userAuth.getUserId());
        String refreshToken = jwtProvider.createRefreshToken(userAuth.getUserId());

        RefreshToken refreshEntity = new RefreshToken();
        refreshEntity.setUserId(userAuth.getUserId());
        refreshEntity.setToken(accessToken);
        refreshEntity.setExpiresAt(Instant.now().plusSeconds(REFRESH_TTL));
        refreshEntity.setRevoked(false);
        refreshTokenRepository.save(refreshEntity);

        return new TokenResponse(accessToken, refreshToken);
    }

    // Access + Refresh Token 쿠키를 한 번에 추가
    // 비회원도 서비스 이용 가능하게 되면, 인증이 필요없는 경우도 있으니 access token을 쿠키 말고 바디에 담아 보내도록 수정 가능
    private void addTokenCookies(HttpServletResponse response, TokenResponse tokenResponse) {
        addTokenCookie(response, "accessToken", tokenResponse.accessToken(), ACCESS_TTL);
        addTokenCookie(response, "refreshToken", tokenResponse.refreshToken(), REFRESH_TTL);
    }

    // 공통 쿠키 생성 로직
    private void addTokenCookie(HttpServletResponse response, String name, String value, int maxAge) {
        Cookie cookie = new Cookie(name, value);
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        cookie.setMaxAge(maxAge);
        response.addCookie(cookie);
    }

    public record TokenResponse(String accessToken, String refreshToken) {}
}
