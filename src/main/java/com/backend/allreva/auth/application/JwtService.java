package com.backend.allreva.auth.application;

import com.backend.allreva.auth.domain.RefreshToken;
import com.backend.allreva.auth.domain.RefreshTokenRepository;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.Date;
import javax.crypto.SecretKey;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Slf4j
@Service
public class JwtService {

    private final SecretKey secretKey;
    private final int ACCESS_TIME;
    private final int REFRESH_TIME;

    private final RefreshTokenRepository refreshTokenRepository;

    public JwtService(
            @Value("${jwt.secret-key}") final String secretKey,
            @Value("${jwt.access.expiration}") final int ACCESS_TIME,
            @Value("${jwt.refresh.expiration}") final int REFRESH_TIME,
            final RefreshTokenRepository refreshTokenRepository
    ) {
        this.secretKey = Keys.hmacShaKeyFor(Decoders.BASE64URL.decode(secretKey));
        this.ACCESS_TIME = ACCESS_TIME;
        this.REFRESH_TIME = REFRESH_TIME;
        this.refreshTokenRepository = refreshTokenRepository;
    }

    /**
     * header에 있는 Access Token을 추출합니다.
     * @param request HTTP 요청
     * @return Access Token String 값
     */
    public String extractAccessToken(final HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");

        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }

    /**
     * cookie 혹은 header에 있는 Refresh Token을 추출합니다.
     * @param request HTTP 요청
     * @return Refresh Token String 값
     */
    public String extractRefreshToken(final HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();

        String refreshToken = null;
        if (cookies != null && cookies.length != 0) {
            refreshToken = Arrays.stream(cookies)
                    .filter(cookie -> cookie.getName().equals("refreshToken"))
                    .findFirst()
                    .map(Cookie::getValue)
                    .orElse(null);
        }

        // Cookie에 없다면 Header 재확인
        if (refreshToken != null) {
            return refreshToken;
        } else {
            return request.getHeader("refresh_token");
        }
    }

    /**
     * 토큰에서 memberId를 추출합니다.
     * @param token 토큰
     * @return memberId String 값
     */
    public String extractMemberId(final String token) {
        return Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload()
                    .getSubject();
    }

    /**
     * 토큰을 검증합니다.
     * @param token 토큰
     * @return 토큰이 유효하면 true, 그렇지 않으면 false
     */
    public boolean validateToken(final String token) {
        if (token == null) {
            return false;
        }
        try {
            Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parse(token);
            return true;
        } catch (SignatureException e) {
            log.error("Invalid JWT token signature: {}", e.getMessage());
        } catch (MalformedJwtException e) {
            log.error("Invalid JWT token: {}", e.getMessage());
        } catch (ExpiredJwtException e) {
            log.error("Expired JWT token: {}", e.getMessage());
        } catch (UnsupportedJwtException e) {
            log.error("JWT token is unsupported: {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            log.error("JWT claims string is empty: {}", e.getMessage());
        }
        return false;
    }

    /**
     * Access Token을 생성합니다.
     * @param subject 토큰에 담을 subject 값
     * @return Access Token String 값
     */
    public String generateAccessToken(final String subject) {
        Date currentDate = new Date();
        Date expireDate = new Date(currentDate.getTime() + ACCESS_TIME);

        return Jwts.builder()
                .subject(subject)
                .issuedAt(currentDate)
                .expiration(expireDate)
                .signWith(secretKey)
                .compact();
    }

    /**
     * Refresh Token을 생성합니다.
     * @param subject 토큰에 담을 subject 값
     * @return Refresh Token String 값
     */
    public String generateRefreshToken(final String subject) {
        Date currentDate = new Date();
        Date expireDate = new Date(currentDate.getTime() + REFRESH_TIME);

        return Jwts.builder()
                .subject(subject)
                .issuedAt(currentDate)
                .expiration(expireDate)
                .signWith(secretKey)
                .compact();
    }

    /**
     * Refresh Token을 Redis에 새로 갱신합니다.
     * @param generatedRefreshToken 새로 생성된 Refresh Token
     * @param memberId 회원 ID
     */
    public void updateRefreshToken(
            final String generatedRefreshToken,
            final String memberId
    ) {
        refreshTokenRepository.findRefreshTokenByMemberId(Long.valueOf(memberId))
                .ifPresent(refreshTokenRepository::delete);

        RefreshToken refreshTokenEntity = RefreshToken.builder()
                .token(generatedRefreshToken)
                .memberId(Long.valueOf(memberId))
                .build();
        refreshTokenRepository.save(refreshTokenEntity);
    }
}
