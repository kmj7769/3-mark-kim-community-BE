package kr.adapterz.community.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
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
    public ResponseEntity<ApiResponseDto<LoginResponseDto>> login(HttpServletRequest request, @RequestBody LoginRequestDto loginRequestDto) {

        Optional<LoginResponseDto> loginResponseDtoOpt = authService.login(loginRequestDto);

        if (loginResponseDtoOpt.isEmpty()) {
            ApiResponseDto<LoginResponseDto> apiResponseDto = new ApiResponseDto<>(
                    HttpStatus.UNAUTHORIZED.value(),
                    "Login failed",
                    null,
                    null
            );
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(apiResponseDto);
        }

        // 세션 생성
        HttpSession session = request.getSession(true);

        // 세션에 회원 id 저장
        session.setAttribute("userId", loginResponseDtoOpt.get().getUserId());

        // 세션 타임아웃 설정 (30분)
        session.setMaxInactiveInterval(1800);

        ApiResponseDto<LoginResponseDto> apiResponseDto = new ApiResponseDto<>(
                HttpStatus.OK.value(),
                "Login success.",
                null,
                loginResponseDtoOpt.get()
        );

        return ResponseEntity.status(HttpStatus.OK).body(apiResponseDto);
    }

    @DeleteMapping
    public ResponseEntity<ApiResponseDto<LoginResponseDto>> logout(HttpServletRequest request) {
        HttpSession session = request.getSession(false);

        if (session != null) {
            session.invalidate();
        }
        else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new ApiResponseDto<>(
                    HttpStatus.UNAUTHORIZED.value(),
                    "There's no session.",
                    null,
                    null
            ));
        }

        return ResponseEntity.status(HttpStatus.OK).body(new ApiResponseDto<>(
                HttpStatus.OK.value(),
                "Logout success.",
                null,
                null
        ));
    }
}
