package com.vasquezhouse.pocservicevirtualization.proxies;

import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

public class PostServiceProxy {

    private final WebClient webClient;

    public PostServiceProxy(WebClient webClient) {
        this.webClient = webClient;
    }

    public Mono<PostInfo> findByPostId(String postId) {

        Mono<ClientResponse> response = webClient
                .get()
                .uri("/posts/{postId}", postId)
                .exchange();

        return response.flatMap(resp -> {
            switch (resp.statusCode()) {
                case OK:
                    return resp.bodyToMono(PostInfo.class);
                case NOT_FOUND:
                    return Mono.error(new PostNotFoundException());
                default:
                    return Mono.error(new RuntimeException("Unknown [" + resp.statusCode() + "]"));
            }
        });
    }
}
