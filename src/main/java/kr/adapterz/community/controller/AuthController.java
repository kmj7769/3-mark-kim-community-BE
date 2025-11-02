package kr.adapterz.community.controller;

import jakarta.servlet.http.HttpServletResponse;
import kr.adapterz.community.dto.ApiResponseDto;
import kr.adapterz.community.dto.auth.LoginRequestDto;
import kr.adapterz.community.dto.auth.LoginResponseDto;
import kr.adapterz.community.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;

    @Autowired
    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    // 로그인 요청 작업을 처리하는 메서드
    @PostMapping
    public ResponseEntity<ApiResponseDto<LoginResponseDto>> login(@RequestBody LoginRequestDto loginRequestDto, HttpServletResponse response) {

        Optional<LoginResponseDto> loginResponseDtoOpt = authService.login(loginRequestDto, response);

        if (loginResponseDtoOpt.isEmpty()) {
            ApiResponseDto<LoginResponseDto> apiResponseDto = new ApiResponseDto<>(
                    HttpStatus.UNAUTHORIZED.value(),
                    "Login failed",
                    null,
                    null
            );
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(apiResponseDto);
        }

        ApiResponseDto<LoginResponseDto> apiResponseDto = new ApiResponseDto<>(
                HttpStatus.OK.value(),
                "Login success.",
                null,
                loginResponseDtoOpt.get()
        );

        return ResponseEntity.status(HttpStatus.OK).body(apiResponseDto);
    }

    @PostMapping("/refresh")
    public ResponseEntity<ApiResponseDto<Object>> refresh(
            @CookieValue(value = "refreshToken", required = false) String refreshToken,
            HttpServletResponse response) {

        if (refreshToken == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new ApiResponseDto<>(
                    HttpStatus.UNAUTHORIZED.value(),
                    "There's no refresh token.",
                    null,
                    null
            ));
        }

        if (!authService.refreshTokens(refreshToken, response)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new ApiResponseDto<>(
                    HttpStatus.UNAUTHORIZED.value(),
                    "Refresh token failed.",
                    null,
                    null
            ));
        }

        return ResponseEntity.status(HttpStatus.OK).body(new ApiResponseDto<>(
                HttpStatus.OK.value(),
                "Refresh token success.",
                null,
                null
        ));
    }

    @DeleteMapping
    public ResponseEntity<ApiResponseDto<Object>> logout(HttpServletResponse response) {

        authService.logout(response);

        return ResponseEntity.status(HttpStatus.OK).body(new ApiResponseDto<>(
                HttpStatus.OK.value(),
                "Logout success.",
                null,
                null
        ));
    }

}
