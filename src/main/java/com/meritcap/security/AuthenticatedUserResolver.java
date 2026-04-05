package com.meritcap.security;

import com.meritcap.exception.CustomException;
import com.meritcap.model.User;
import com.meritcap.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Locale;

@Slf4j
@Component
@RequiredArgsConstructor
public class AuthenticatedUserResolver {

    private final UserRepository userRepository;

    public User resolveCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext() != null
                ? SecurityContextHolder.getContext().getAuthentication()
                : null;

        if (authentication == null || !authentication.isAuthenticated()
                || authentication instanceof AnonymousAuthenticationToken) {
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

        User user = resolveUserFromPrincipal(principal);

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

    public Long resolveCurrentStudentId() {
        User user = resolveCurrentUser();
        if (user.getStudent() == null || user.getStudent().getId() == null) {
            throw new CustomException("Authenticated student not found");
        }
        return user.getStudent().getId();
    }

    public void assertCurrentStudentOwns(Long requestedStudentId) {
        Long currentStudentId = resolveCurrentStudentId();
        if (!currentStudentId.equals(requestedStudentId)) {
            log.warn("Forbidden student access. currentStudentId={}, requestedStudentId={}", currentStudentId,
                    requestedStudentId);
            throw new CustomException("You do not have permission to access this resource");
        }
    }

    public boolean isCurrentUserAdmin() {
        User user = resolveCurrentUser();
        return user.getRole() != null && user.getRole().getName() != null
                && "ADMIN".equalsIgnoreCase(user.getRole().getName());
    }

    private User resolveUserFromPrincipal(String principal) {
        User user = null;
        if (principal.contains("@")) {
            user = userRepository.findByEmailIgnoreCase(principal);
        }
        if (user == null) {
            user = userRepository.findByUsernameIgnoreCase(principal);
        }
        if (user == null && principal.contains("@")) {
            user = userRepository.findByEmail(principal.toLowerCase(Locale.ROOT));
        }
        return user;
    }
}
