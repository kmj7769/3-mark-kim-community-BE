package kr.adapterz.community.service;

import jakarta.servlet.http.HttpServletResponse;
import kr.adapterz.community.dto.auth.LoginRequestDto;
import kr.adapterz.community.dto.auth.LoginResponseDto;

import java.util.Optional;

public interface AuthService {
    /*
        로그인 요청 시 호출
        유저의 인증 정보와 요청 바디의 email과 password를 비교
        일치할 시 응답 DTO 반환
     */
    Optional<LoginResponseDto> login(LoginRequestDto loginRequestDto, HttpServletResponse response);

    /*
        access 토큰 재발급 시 호출
        access / refresh 모두 토큰 재발급
        재발급 성공 시 true, 실패 시 false 반환
     */
    Boolean refreshTokens(String refreshToken, HttpServletResponse response);

    /*
        로그아웃 요청 시 호출
        쿠키의 토큰 유효 수명을 0으로 초기화
     */
    void logout(HttpServletResponse response);

}
