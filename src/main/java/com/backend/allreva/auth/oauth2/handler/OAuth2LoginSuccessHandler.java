package com.backend.allreva.auth.oauth2.handler;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import com.backend.allreva.auth.application.dto.PrincipalDetails;
import com.backend.allreva.auth.application.dto.LoginSuccessResponse;
import com.backend.allreva.auth.util.JwtProvider;
import com.backend.allreva.common.dto.Response;
import com.backend.allreva.member.command.domain.Member;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class OAuth2LoginSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JwtProvider jwtProvider;
    private final ObjectMapper objectMapper;
    @Value("${jwt.refresh.expiration}")
    private Long REFRESH_TIME;

    /**
     * OAuth2 인증 success시 JWT 반환하는 메서드
     *
     * OAuth2는 OAuth2UserService에서 이미 인증되기 때문에 별도의 인증 filter가 필요없다.
     */
    @Override
    public void onAuthenticationSuccess(
            final HttpServletRequest request,
            final HttpServletResponse response,
            final Authentication authentication
    ) throws IOException, ServletException {
        // OAuth2 인증된 사용자 정보 가져오기
        PrincipalDetails oAuth2User = (PrincipalDetails) authentication.getPrincipal();
        Member member = oAuth2User.member();

        // token 생성
        String memberId = String.valueOf(member.getId());
        String accessToken = jwtProvider.generateAccessToken(memberId);
        String refreshToken = jwtProvider.generateRefreshToken(memberId);

        // access token 응답객체 생성
        LoginSuccessResponse loginSuccessResponse = LoginSuccessResponse.of(
                accessToken,
                refreshToken,
                jwtProvider.getREFRESH_TIME(),
                member.getEmail().getEmail(),
                member.getMemberInfo().getProfileImageUrl());

        // TODO: db or cache에 RefreshToken 저장

        // refreshToken 쿠키 등록
        setHeader(response, refreshToken);

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        Response<LoginSuccessResponse> apiResponse = Response.onSuccess(loginSuccessResponse);
        String jsonResponse = objectMapper.writeValueAsString(apiResponse);
        response.getWriter().write(jsonResponse);
    }

    // refreshToken 쿠키 설정
    public void setHeader(final HttpServletResponse response, final String refreshToken) {
        if (refreshToken != null) {
            response.addHeader("refresh_token", refreshToken);
            response.addHeader("Set-Cookie", createRefreshToken(refreshToken).toString());
        }
    }

    // refreshToken 쿠키 생성
    public ResponseCookie createRefreshToken(final String refreshToken) {
        return ResponseCookie.from("refreshToken", refreshToken)
                .path("/")
                .maxAge(REFRESH_TIME)
                .httpOnly(true)
                .build();
    }
}