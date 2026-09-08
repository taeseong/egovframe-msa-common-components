package egovframework.com.uat.uia.util;

import egovframework.com.uat.uia.service.LoginVO;
import org.egovframe.boot.crypto.service.impl.EgovEnvCryptoServiceImpl;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Base64;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class EgovJwtProviderTest {

    @Test
    void extractAuthLsReadsAuthorityClaimFromRefreshToken() {
        EgovEnvCryptoServiceImpl cryptoService = mock(EgovEnvCryptoServiceImpl.class);
        EgovJwtProvider jwtProvider = new EgovJwtProvider(cryptoService);
        ReflectionTestUtils.setField(jwtProvider, "refreshSecret",
                Base64.getEncoder().encodeToString(new byte[32]));
        ReflectionTestUtils.setField(jwtProvider, "refreshExpiration", "60000");

        LoginVO loginVO = new LoginVO();
        loginVO.setAuthorList("ROLE_USER");
        when(cryptoService.encrypt("ROLE_USER")).thenReturn("encrypted-authorities");

        String refreshToken = jwtProvider.createRefreshToken(loginVO);

        assertThat(jwtProvider.extractAuthLs(refreshToken)).isEqualTo("encrypted-authorities");
    }
}
