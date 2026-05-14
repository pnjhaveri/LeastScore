package com.example.leastscore.service;

import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import java.util.Map;

@Service
public class GitHubService {
  private final WebClient webClient;

  public GitHubService() {
    this.webClient = WebClient.builder()
        .baseUrl("https://api.github.com")
        .defaultHeader("Accept", "application/vnd.github+json")
        .defaultHeader("X-GitHub-Api-Version", "2022-11-28")
        .build();
  }

  public record GitHubUser(String login, long id, String avatarUrl) {}

  public Mono<GitHubUser> validateTokenAndGetUser(String token) {
    return webClient.get()
        .uri("/user")
        .header("Authorization", "Bearer " + token)
        .retrieve()
        .bodyToMono(Map.class)
        .map(response -> {
          Map<?, ?> userMap = (Map<?, ?>) response;
          return new GitHubUser(
            (String) userMap.get("login"),
            ((Number) userMap.get("id")).longValue(),
            (String) userMap.get("avatar_url")
          );
        })
        .onErrorResume(e -> Mono.error(new RuntimeException("Invalid GitHub token")));
  }
}
