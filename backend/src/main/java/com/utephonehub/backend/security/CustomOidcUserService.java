package com.utephonehub.backend.security;

import com.utephonehub.backend.entity.Cart;
import com.utephonehub.backend.entity.User;
import com.utephonehub.backend.enums.UserRole;
import com.utephonehub.backend.enums.UserStatus;
import com.utephonehub.backend.exception.UnauthorizedException;
import com.utephonehub.backend.repository.CartRepository;
import com.utephonehub.backend.repository.UserRepository;
import com.utephonehub.backend.util.PasswordEncoder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class CustomOidcUserService extends OidcUserService {

    private final UserRepository userRepository;
    private final CartRepository cartRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public OidcUser loadUser(OidcUserRequest userRequest) {
        OidcUser oidcUser = super.loadUser(userRequest);

        String email = oidcUser.getEmail();
        if (email == null || email.isBlank()) {
            log.error("Google account does not provide email, cannot proceed with OAuth2 login");
            throw new UnauthorizedException("Không thể đăng nhập bằng Google: tài khoản không có email");
        }

        User user = userRepository.findByEmail(email).orElse(null);

        if (user != null) {
            if (user.getStatus() == UserStatus.LOCKED) {
                throw new UnauthorizedException("Tài khoản của bạn đã bị khóa");
            }
            log.info("Existing user logged in via Google: {}", email);
            return oidcUser;
        }

        String fullName = oidcUser.getFullName();
        if (fullName == null || fullName.isBlank()) {
            fullName = email.substring(0, email.indexOf('@'));
        }

        String baseUsername = email.substring(0, email.indexOf('@'));
        String username = baseUsername;
        int counter = 1;
        while (userRepository.existsByUsername(username)) {
            username = baseUsername + "_" + counter++;
        }

        String randomPassword = UUID.randomUUID().toString();

        // Create new user with ACTIVE status
        User newUser = User.builder()
                .email(email)
                .fullName(fullName)
                .username(username)
                .passwordHash(passwordEncoder.encode(randomPassword))
                .role(UserRole.CUSTOMER)
                .status(UserStatus.ACTIVE)
                .build();

        newUser = userRepository.save(newUser);

        Cart cart = Cart.builder()
                .user(newUser)
                .build();
        cartRepository.save(cart);

        log.info("Created new user via Google login with id: {}", newUser.getId());

        return oidcUser;
    }
}
