package egovframework.com.filter;

import egovframework.com.config.GatewayJwtProvider;
import org.junit.jupiter.api.Test;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AuthGatewayFilterFactoryTest {

    @Test
    void getParamFromPathReturnsLoginCategoryForMobileIdPath() {
        AuthGatewayFilterFactory filterFactory = new AuthGatewayFilterFactory(
                WebClient.builder(), mock(GatewayJwtProvider.class));

        String pathCode = ReflectionTestUtils.invokeMethod(
                filterFactory, "getParamFromPath", "/mip/main");

        assertThat(pathCode).isEqualTo("1");
    }

    @Test
    @SuppressWarnings({"rawtypes", "unchecked"})
    void regenerateAccessTokenSetsSecureCookie() {
        WebClient.Builder webClientBuilder = mock(WebClient.Builder.class);
        WebClient webClient = mock(WebClient.class);
        WebClient.RequestHeadersUriSpec requestSpec = mock(WebClient.RequestHeadersUriSpec.class);
        WebClient.RequestHeadersSpec headersSpec = mock(WebClient.RequestHeadersSpec.class);
        WebClient.ResponseSpec responseSpec = mock(WebClient.ResponseSpec.class);
        GatewayJwtProvider jwtProvider = mock(GatewayJwtProvider.class);

        when(webClientBuilder.build()).thenReturn(webClient);
        when(webClient.get()).thenReturn(requestSpec);
        when(requestSpec.uri(anyString())).thenReturn(headersSpec);
        when(headersSpec.header(anyString(), any(String[].class))).thenReturn(headersSpec);
        when(headersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(Map.class))
                .thenReturn(Mono.just(Map.of("accessToken", "new-access-token")));
        when(jwtProvider.accessValidateToken("new-access-token")).thenReturn(200);
        when(jwtProvider.getAccessExpiration()).thenReturn("60000");
        when(jwtProvider.extractAuthLs("new-access-token")).thenReturn("/main/**");

        MockServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.get("/main/").build());
        when(jwtProvider.headerSetting(exchange, "new-access-token"))
                .thenReturn(exchange.getRequest());
        GatewayFilterChain chain = mock(GatewayFilterChain.class);
        when(chain.filter(any(ServerWebExchange.class))).thenReturn(Mono.empty());
        AuthGatewayFilterFactory filterFactory = new AuthGatewayFilterFactory(webClientBuilder, jwtProvider);

        Mono<Void> result = ReflectionTestUtils.invokeMethod(
                filterFactory, "regenerateAccessToken", exchange, "refresh-token", chain);
        result.block();

        ResponseCookie cookie = exchange.getResponse().getCookies().getFirst("accessToken");
        assertThat(cookie).isNotNull();
        assertThat(cookie.isSecure()).isTrue();
        assertThat(exchange.getResponse().getStatusCode()).isNotEqualTo(HttpStatus.FOUND);
    }
}
