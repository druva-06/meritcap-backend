package com.meritcap.security;

import com.meritcap.exception.CustomException;
import com.meritcap.model.User;
import com.meritcap.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class AuthenticatedUserResolver {

    private final UserRepository userRepository;

    public User resolveCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext() != null
                ? SecurityContextHolder.getContext().getAuthentication()
                : null;

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new CustomException("Authentication required");
        }

        String principal = authentication.getName();
        if (principal == null || principal.isBlank()) {
            throw new CustomException("Unable to resolve authenticated user");
        }
        if (principal.startsWith("otp-user-")) {
            String idPart = principal.substring("otp-user-".length());
            try {
                Long userId = Long.parseLong(idPart);
                return userRepository.findById(userId)
                        .orElseThrow(() -> new CustomException("Authenticated user not found"));
            } catch (NumberFormatException ignored) {
                // fallback to regular resolution
            }
        }

        User user = userRepository.findByEmailIgnoreCase(principal);
        if (user == null) {
            user = userRepository.findByUsername(principal);
        }
        if (user == null && principal.contains("@")) {
            user = userRepository.findByEmail(principal.toLowerCase());
        }

        if (user == null) {
            throw new CustomException("Authenticated user not found");
        }
        return user;
    }

    public Long resolveCurrentUserId() {
        return resolveCurrentUser().getId();
    }

    public void assertCurrentUserOwns(Long requestedUserId) {
        Long currentUserId = resolveCurrentUserId();
        if (!currentUserId.equals(requestedUserId)) {
            log.warn("Forbidden user access. currentUserId={}, requestedUserId={}", currentUserId, requestedUserId);
            throw new CustomException("You do not have permission to access this resource");
        }
    }
}
