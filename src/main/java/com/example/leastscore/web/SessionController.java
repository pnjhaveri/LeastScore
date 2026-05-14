package com.example.leastscore.web;

import com.example.leastscore.db.UserEntity;
import com.example.leastscore.service.IdentityService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/session")
@Validated
public class SessionController {
  private final IdentityService identityService;

  public SessionController(IdentityService identityService) {
    this.identityService = identityService;
  }

  @GetMapping
  public ResponseEntity<?> getSession(HttpSession session) {
    Long userId = identityService.getCurrentUserId(session);
    String username = null;
    if (userId != null) {
      UserEntity u = identityService.getUserById(userId);
      if (u != null) {
        username = u.getUsername();
      }
    }
    return ResponseEntity.ok(new SessionResponse(userId, username));
  }

  @PostMapping("/username")
  public ResponseEntity<?> setUsername(@RequestBody @Validated SetUsernameRequest req, HttpSession session) {
    UserEntity u = identityService.ensureUser(session, req.username());
    return ResponseEntity.ok(new SetUsernameResponse(u.getId(), u.getUsername()));
  }

  public record SetUsernameRequest(@NotBlank String username) {}

  public record SetUsernameResponse(long userId, String username) {}

  public record SessionResponse(Long userId, String username) {}
}

