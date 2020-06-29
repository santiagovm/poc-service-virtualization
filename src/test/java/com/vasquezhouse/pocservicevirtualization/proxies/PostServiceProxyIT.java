package com.vasquezhouse.pocservicevirtualization.proxies;

import com.vasquezhouse.pocservicevirtualization.ServiceVirtualizationPoCApplication;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        classes = ServiceVirtualizationPoCApplication.class
)
@AutoConfigureWireMock(port = 0) // 0 = use random port
public class PostServiceProxyIT {

    private PostServiceProxy postService;

    @Autowired
    Environment env;

    @BeforeEach
    public void setup() {

        String wiremockPort = env.getProperty("wiremock.server.port");
        String baseUrl = "http://localhost:" + wiremockPort;

        WebClient webClient = WebClient
                .builder()
                .clone()
                .baseUrl(baseUrl)
                .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                .build();

        postService = new PostServiceProxy(webClient);
    }

    @Test
    public void verifyExistingPost() {

        // act
        PostInfo post = postService.findByPostId("333").block();

        // assert
        assertEquals("333", post.getId());
        assertEquals("foo title", post.getTitle());
    }

    @Test
    public void verifyNonExistingPost() {

        // act and assert
        Exception exception = assertThrows(
                PostNotFoundException.class,
                () -> postService.findByPostId("444").block()
        );
    }

    @Test
    public void verifyInternalError() {

        // act and assert
        Exception exception = assertThrows(
                RuntimeException.class,
                () -> postService.findByPostId("555").block()
        );
    }
}
