package kr.adapterz.community.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class SessionAuthFilter extends OncePerRequestFilter {

    // 필터 제외 요청 설정
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getServletPath();
        String method = request.getMethod();

        // 로그인 요청 제외
        if (path.equals("/auth") && method.equals("POST")) {
            return true;
        }

        // 회원가입 요청 제외
        if (path.equals("/users") && method.equals("POST")) {
            return true;
        }

        return false;
    }

    // 실제 필터 로직
    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain chain
            ) throws IOException, ServletException {

        // 요청에서 세션 가져오기
        HttpSession session = request.getSession(false);

        // 세션 정보가 없으면 인증 실패 처리
        if (session == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("Login is required.");
            return;
        }

        // 세션 정보에 회원 id가 없으면 인증 실패 처리
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("Login is required.");
            return;
        }

        // 요청에 회원 id 넘겨주기
        request.setAttribute("userId", userId);

        // 다음 필터 실행
        chain.doFilter(request, response);
    }
}
