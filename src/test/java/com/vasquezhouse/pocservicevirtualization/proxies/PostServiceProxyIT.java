package com.vasquezhouse.pocservicevirtualization.proxies;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.recording.RecordSpec;
import com.github.tomakehurst.wiremock.recording.RecordingStatus;
import com.vasquezhouse.pocservicevirtualization.ServiceVirtualizationPoCApplication;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;

import static com.github.tomakehurst.wiremock.client.WireMock.recordSpec;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        classes = ServiceVirtualizationPoCApplication.class
)
public class PostServiceProxyIT {

    @Value("${postServiceBaseUrl}")
    private String postServiceBaseUrl;

    @Value("${wiremock.useMockResponses:true}")
    private boolean useMockResponses;

    @Value("${wiremock.makeStubsPersistent:false}")
    private boolean persistRecordings;

    @Value("${wiremock.maxTextBodySize:0}")
    private int maxTextBodySize;

    private PostServiceProxy postService;

    WireMockServer wireMockServer;

    @BeforeEach
    public void setup() {

        RecordSpec theRecordSpec = recordSpec()
                .forTarget(postServiceBaseUrl)
                .makeStubsPersistent(persistRecordings)
                .extractTextBodiesOver(maxTextBodySize)
                .captureHeader("Content-Type")
                .build();

        final int WIREMOCK_RANDOM_PORT = 0;
        wireMockServer = new WireMockServer(WIREMOCK_RANDOM_PORT);
        wireMockServer.start();

        int wireMockPort = wireMockServer.port();
        String wireMockBaseUrl = "http://localhost:" + wireMockPort;

        if (!useMockResponses) {
            wireMockServer.startRecording(theRecordSpec);
        }

        WebClient webClient = WebClient
                .builder()
                .clone()
                .baseUrl(wireMockBaseUrl)
                .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                .build();

        postService = new PostServiceProxy(webClient);
    }

    @AfterEach
    public void tearDown() {

        if (wireMockServer.getRecordingStatus().getStatus().equals(RecordingStatus.Recording)) {
            wireMockServer.stopRecording();
        }

        wireMockServer.stop();
    }

    @Test
    public void verifyExistingPost() {

        // act
        String expectedPostId = "1";
        PostInfo post = postService.findByPostId(expectedPostId).block();

        // assert
        String expectedTitle = "sunt aut facere repellat provident occaecati excepturi optio reprehenderit";

        assertEquals(expectedPostId, post.getId());
        assertEquals(expectedTitle, post.getTitle());
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
