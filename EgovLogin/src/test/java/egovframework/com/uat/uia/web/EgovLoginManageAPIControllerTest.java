package egovframework.com.uat.uia.web;

import egovframework.com.uat.uia.service.EgovLoginManageService;
import egovframework.com.uat.uia.token.AuthorizeToken;
import egovframework.com.uat.uia.token.AuthorizeTokenService;
import egovframework.com.uat.uia.util.EgovJwtProvider;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.egovframe.boot.security.bean.EgovReloadableFilterInvocationSecurityMetadataSource;
import org.junit.jupiter.api.Test;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;

import javax.crypto.SecretKey;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class EgovLoginManageAPIControllerTest {

    @Test
    void recreateAccessTokenKeepsAuthLsClaim() {
        EgovJwtProvider jwtProvider = mock(EgovJwtProvider.class);
        AuthorizeTokenService tokenService = mock(AuthorizeTokenService.class);
        EgovLoginManageAPIController controller = new EgovLoginManageAPIController(
                mock(EgovLoginManageService.class),
                jwtProvider,
                mock(ReloadableResourceBundleMessageSource.class),
                mock(EgovReloadableFilterInvocationSecurityMetadataSource.class),
                mock(AuthenticationManager.class),
                tokenService);

        String refreshToken = "refresh-token";
        AuthorizeToken authorizeToken = new AuthorizeToken(
                "USER", "old-token-key", refreshToken, "", "");
        SecretKey accessKey = Keys.hmacShaKeyFor(new byte[32]);

        when(jwtProvider.extractUserId(refreshToken)).thenReturn("encrypted-user-id");
        when(jwtProvider.decrypt("encrypted-user-id")).thenReturn("USER");
        when(jwtProvider.extractUserNm(refreshToken)).thenReturn("encrypted-user-name");
        when(jwtProvider.extractUniqId(refreshToken)).thenReturn("encrypted-uniq-id");
        when(jwtProvider.extractAuthLs(refreshToken)).thenReturn("encrypted-authorities");
        when(jwtProvider.getAccessExpiration()).thenReturn("60000");
        when(jwtProvider.getRefreshExpiration()).thenReturn("120000");
        when(jwtProvider.getAccessSecret()).thenReturn("access-secret");
        when(jwtProvider.getSigningKey("access-secret")).thenReturn(accessKey);
        when(jwtProvider.generateHash(anyString())).thenReturn("new-token-key");
        when(tokenService.findToken("USER")).thenReturn(authorizeToken);

        ResponseEntity<?> response = controller.recreateAccessToken(refreshToken);

        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        Map<?, ?> body = (Map<?, ?>) response.getBody();
        String accessToken = (String) body.get("accessToken");
        Claims claims = Jwts.parser().verifyWith(accessKey).build()
                .parseSignedClaims(accessToken).getPayload();
        assertThat(claims.get("authLs")).isEqualTo("encrypted-authorities");
        assertThat(claims.get("authId")).isNull();
    }
}
