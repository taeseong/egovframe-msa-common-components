package egovframework.com.uat.uia.web;

import egovframework.com.uat.uia.service.EgovLoginManageService;
import egovframework.com.uat.uia.service.LoginDTO;
import egovframework.com.uat.uia.service.LoginVO;
import egovframework.com.uat.uia.token.AuthorizeTokenService;
import egovframework.com.uat.uia.util.EgovJwtProvider;
import org.egovframe.boot.security.bean.EgovReloadableFilterInvocationSecurityMetadataSource;
import org.egovframe.boot.security.userdetails.util.EgovUserDetailsHelper;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.http.ResponseCookie;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class EgovLoginManageAPIControllerTest {

    @Test
    void actionLoginAddsAccessAndRefreshTokenCookies() {
        EgovLoginManageService loginService = mock(EgovLoginManageService.class);
        EgovJwtProvider jwtProvider = mock(EgovJwtProvider.class);
        AuthenticationManager authenticationManager = mock(AuthenticationManager.class);
        AuthorizeTokenService tokenService = mock(AuthorizeTokenService.class);
        EgovLoginManageAPIController controller = new EgovLoginManageAPIController(
                loginService,
                jwtProvider,
                mock(ReloadableResourceBundleMessageSource.class),
                mock(EgovReloadableFilterInvocationSecurityMetadataSource.class),
                authenticationManager,
                tokenService);
        ReflectionTestUtils.setField(controller, "lock", "false");

        LoginDTO loginDTO = new LoginDTO();
        loginDTO.setId("USER");
        loginDTO.setName("일반회원");
        loginDTO.setPassword("password");
        loginDTO.setUniqId("USRCNFRM_00000000000");
        when(loginService.actionLogin(any(LoginVO.class))).thenReturn(loginDTO);
        when(authenticationManager.authenticate(any(Authentication.class)))
                .thenReturn(mock(Authentication.class));
        when(jwtProvider.createAccessToken(any(LoginVO.class))).thenReturn("access-token");
        when(jwtProvider.createRefreshToken(any(LoginVO.class))).thenReturn("refresh-token");
        when(jwtProvider.generateHash("access-token")).thenReturn("token-key");
        when(jwtProvider.getAccessExpiration()).thenReturn("60000");
        when(jwtProvider.getRefreshExpiration()).thenReturn("120000");
        when(jwtProvider.createCookie("accessToken", "access-token", 120))
                .thenReturn(ResponseCookie.from("accessToken", "access-token").httpOnly(true).build());
        when(jwtProvider.createCookie("refreshToken", "refresh-token", 180))
                .thenReturn(ResponseCookie.from("refreshToken", "refresh-token").httpOnly(true).build());

        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        try (MockedStatic<EgovUserDetailsHelper> userDetails = mockStatic(EgovUserDetailsHelper.class)) {
            userDetails.when(EgovUserDetailsHelper::getRoleAndPatternList).thenReturn(List.of());
            userDetails.when(EgovUserDetailsHelper::getAuthorities).thenReturn(List.of());
            userDetails.when(() -> EgovUserDetailsHelper.getAccessiblePatterns(List.of(), List.of()))
                    .thenReturn("/main/**");

            controller.actionLogin(new LoginVO(), request, response);
        }

        assertThat(response.getHeaders("Set-Cookie"))
                .extracting(header -> header.substring(0, header.indexOf('=')))
                .containsExactly("accessToken", "refreshToken");
        verify(jwtProvider).createCookie("refreshToken", "refresh-token", 180);
    }
}
