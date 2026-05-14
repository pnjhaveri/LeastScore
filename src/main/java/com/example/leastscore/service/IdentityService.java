package com.example.leastscore.service;

import com.example.leastscore.db.UserEntity;
import com.example.leastscore.db.UserRepository;
import jakarta.servlet.http.HttpSession;
import java.time.Instant;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class IdentityService {
  private static final String SESSION_USER_ID = "LEASTSCORE_USER_ID";

  private final UserRepository userRepository;

  public IdentityService(UserRepository userRepository) {
    this.userRepository = userRepository;
  }

  public Long getCurrentUserId(HttpSession session) {
    Object v = session.getAttribute(SESSION_USER_ID);
    return (v instanceof Long l) ? l : null;
  }

  public UserEntity getUserById(Long userId) {
    if (userId == null) return null;
    return userRepository.findById(userId).orElse(null);
  }

  public UserEntity ensureUser(HttpSession session, String username) {
    if (!StringUtils.hasText(username)) {
      throw new IllegalArgumentException("username is required");
    }
    String trimmedUsername = username.trim();
    Long existingId = getCurrentUserId(session);
    if (existingId != null) {
      UserEntity existing = userRepository.findById(existingId).orElse(null);
      if (existing != null) {
        existing.setLastLoginAt(Instant.now());
        return userRepository.save(existing);
      }
    }
    return userRepository.findByUsername(trimmedUsername).map(user -> {
      user.setLastLoginAt(Instant.now());
      UserEntity saved = userRepository.save(user);
      session.setAttribute(SESSION_USER_ID, saved.getId());
      return saved;
    }).orElseGet(() -> createAndStore(session, trimmedUsername));
  }

  private UserEntity createAndStore(HttpSession session, String username) {
    var user = new UserEntity();
    user.setUsername(username);
    user.setLastLoginAt(Instant.now());
    user = userRepository.save(user);
    session.setAttribute(SESSION_USER_ID, user.getId());
    return user;
  }
}

