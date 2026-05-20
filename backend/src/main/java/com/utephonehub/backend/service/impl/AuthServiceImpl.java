package com.utephonehub.backend.service.impl;

import com.utephonehub.backend.dto.request.auth.*;
import com.utephonehub.backend.dto.response.auth.AuthResponse;
import com.utephonehub.backend.dto.response.user.UserResponse;
import com.utephonehub.backend.entity.User;
import com.utephonehub.backend.entity.Cart;
import com.utephonehub.backend.enums.UserRole;
import com.utephonehub.backend.enums.UserStatus;
import com.utephonehub.backend.exception.BadRequestException;
import com.utephonehub.backend.exception.ConflictException;
import com.utephonehub.backend.exception.ResourceNotFoundException;
import com.utephonehub.backend.exception.UnauthorizedException;
import com.utephonehub.backend.repository.UserRepository;
import com.utephonehub.backend.repository.CartRepository;
import com.utephonehub.backend.service.IAuthService;
import com.utephonehub.backend.service.IEmailService;
import com.utephonehub.backend.mapper.UserMapper;
import com.utephonehub.backend.util.JwtTokenProvider;
import com.utephonehub.backend.util.PasswordEncoder;
import com.utephonehub.backend.util.OtpGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImpl implements IAuthService {

    private final UserRepository userRepository;
    private final CartRepository cartRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final OtpGenerator otpGenerator;
    private final RedisTemplate<String, String> redisTemplate;
    private final IEmailService emailService;
    private final UserMapper userMapper;

    private static final String OTP_PREFIX = "otp:";
    private static final String REGISTER_OTP_PREFIX = "verify_email:";
    private static final long OTP_EXPIRATION_MINUTES = 5;

    @Override
    @Transactional
    public UserResponse register(RegisterRequest request) {
        log.info("Registering new user with email: {}", request.getEmail());

        // Validate password match
        if (request.getPassword() != null && request.getConfirmPassword() != null
                && !request.getPassword().equals(request.getConfirmPassword())) {
            throw new BadRequestException("Mật khẩu và xác nhận mật khẩu không khớp");
        }

        // Check if email already exists
        if (request.getEmail() != null && userRepository.existsByEmail(request.getEmail())) {
            throw new ConflictException("Email này đã được sử dụng");
        }

        // Check if username already exists
        if (request.getUsername() != null && userRepository.existsByUsername(request.getUsername())) {
            throw new ConflictException("Tên đăng nhập này đã được sử dụng");
        }

        // Create new user
        User user = User.builder()
                .username(request.getUsername())
                .fullName(request.getFullName())
                .email(request.getEmail())
                .phoneNumber(request.getPhoneNumber())
                .gender(request.getGender())
                .dateOfBirth(request.getDateOfBirth())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .role(UserRole.CUSTOMER)
                .status(UserStatus.ACTIVE)
                .build();

        user = userRepository.save(user);

        // Create cart for user
        Cart cart = Cart.builder()
                .user(user)
                .build();
        cartRepository.save(cart);

        log.info("User registered successfully with id: {}", user.getId());

        // Send welcome registration email (async, không block registration flow)
        try {
            log.info("Attempting to send registration welcome email to: {}", user.getEmail());
            emailService.sendRegistrationEmail(user.getEmail(), user.getFullName());
            log.info("Registration welcome email sent successfully to: {}", user.getEmail());
        } catch (Exception e) {
            log.error("Failed to send registration welcome email to {}: {}",
                    user.getEmail(), e.getMessage(), e);
            // Không throw exception để không ảnh hưởng registration
        }

        return userMapper.toResponse(user);
    }

    @Override
    @Transactional
    public UserResponse registerAdmin(RegisterRequest request) {
        log.info("Registering new admin with email: {}", request.getEmail());

        // Validate password match
        if (request.getPassword() != null && request.getConfirmPassword() != null
                && !request.getPassword().equals(request.getConfirmPassword())) {
            throw new BadRequestException("Mật khẩu và xác nhận mật khẩu không khớp");
        }

        // Check if email already exists
        if (request.getEmail() != null && userRepository.existsByEmail(request.getEmail())) {
            throw new ConflictException("Email này đã được sử dụng");
        }

        // Check if username already exists
        if (request.getUsername() != null && userRepository.existsByUsername(request.getUsername())) {
            throw new ConflictException("Tên đăng nhập này đã được sử dụng");
        }

        // Create new admin user
        User user = User.builder()
                .username(request.getUsername())
                .fullName(request.getFullName())
                .email(request.getEmail())
                .phoneNumber(request.getPhoneNumber())
                .gender(request.getGender())
                .dateOfBirth(request.getDateOfBirth())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .role(UserRole.ADMIN)
                .status(UserStatus.ACTIVE)
                .build();

        user = userRepository.save(user);

        log.info("Admin registered successfully with id: {}", user.getId());

        return userMapper.toResponse(user);
    }

    @Override
    @Transactional
    public AuthResponse login(LoginRequest request) {
        log.info("User login attempt with usernameOrEmail: {}", request.getUsernameOrEmail());

        // Find user by email or username
        User user = userRepository.findByEmail(request.getUsernameOrEmail())
                .or(() -> userRepository.findByUsername(request.getUsernameOrEmail()))
                .orElseThrow(() -> new UnauthorizedException("Tên đăng nhập/email hoặc mật khẩu không chính xác"));

        // Check if account is locked
        if (user.getStatus() == UserStatus.LOCKED) {
            throw new UnauthorizedException("Tài khoản của bạn đã bị khóa");
        }

        // Only allow ACTIVE users to login
        if (user.getStatus() != UserStatus.ACTIVE) {
            throw new UnauthorizedException("Tài khoản của bạn không ở trạng thái hoạt động");
        }

        // Verify password
        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new UnauthorizedException("Tên đăng nhập/email hoặc mật khẩu không chính xác");
        }

        AuthResponse response = buildAuthResponse(user);
        log.info("User logged in successfully with id: {}", user.getId());
        return response;
    }

    @Override
    public AuthResponse refreshToken(RefreshTokenRequest request) {
        log.info("Refreshing token");

        if (!jwtTokenProvider.validateToken(request.getRefreshToken())) {
            throw new UnauthorizedException("Refresh token không hợp lệ");
        }

        Long userId = jwtTokenProvider.getUserIdFromToken(request.getRefreshToken());
        String email = jwtTokenProvider.getEmailFromToken(request.getRefreshToken());

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Người dùng không tồn tại"));

        // Verify refresh token in Redis
        String refreshTokenKey = "refresh_token:" + userId;
        String storedToken = redisTemplate.opsForValue().get(refreshTokenKey);
        if (!request.getRefreshToken().equals(storedToken)) {
            throw new UnauthorizedException("Refresh token không hợp lệ");
        }

        // Generate new access token
        String newAccessToken = jwtTokenProvider.generateAccessToken(userId, email);

        return AuthResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(request.getRefreshToken())
                .tokenType("Bearer")
                .expiresIn(jwtTokenProvider.getExpirationTime() / 1000)
                .user(userMapper.toResponse(user))
                .build();
    }

    /**
     * Generate access/refresh tokens for a user, store refresh token in Redis, and
     * build AuthResponse.
     */
    public AuthResponse buildAuthResponse(User user) {
        String accessToken = jwtTokenProvider.generateAccessToken(user.getId(), user.getEmail());
        String refreshToken = jwtTokenProvider.generateRefreshToken(user.getId(), user.getEmail());

        String refreshTokenKey = "refresh_token:" + user.getId();
        redisTemplate.opsForValue().set(refreshTokenKey, refreshToken, 7, TimeUnit.DAYS);

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(jwtTokenProvider.getExpirationTime() / 1000)
                .user(userMapper.toResponse(user))
                .build();
    }

    @Override
    @Transactional
    public void logout(Long userId) {
        log.info("User logout with id: {}", userId);
        String refreshTokenKey = "refresh_token:" + userId;
        redisTemplate.delete(refreshTokenKey);
    }

    @Override
    @Transactional
    public void requestPasswordReset(ForgotPasswordRequest request) {
        log.info("Password reset request for email: {}", request.getEmail());

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("Người dùng không tồn tại"));

        // Generate OTP
        String otp = otpGenerator.generateOtp();

        // Store OTP in Redis with expiration
        String otpKey = OTP_PREFIX + request.getEmail();
        redisTemplate.opsForValue().set(otpKey, otp, OTP_EXPIRATION_MINUTES, TimeUnit.MINUTES);

        // Send OTP via email
        try {
            emailService.sendOtpEmail(request.getEmail(), otp);
        } catch (Exception e) {
            log.error("Failed to send OTP email: {}", e.getMessage());
            throw new BadRequestException("Không thể gửi email OTP");
        }

        log.info("OTP sent to email: {}", request.getEmail());
    }

    @Override
    @Transactional
    public void verifyOtpAndResetPassword(VerifyOtpRequest request) {
        log.info("Verifying OTP and resetting password for email: {}", request.getEmail());

        // Validate input
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new BadRequestException("Mật khẩu và xác nhận mật khẩu không khớp");
        }

        if (!passwordEncoder.isValidPassword(request.getNewPassword())) {
            throw new BadRequestException("Mật khẩu phải ít nhất 8 ký tự, chứa chữ hoa, chữ thường và số");
        }

        // Verify OTP
        String otpKey = OTP_PREFIX + request.getEmail();
        String storedOtp = redisTemplate.opsForValue().get(otpKey);

        if (storedOtp == null || !storedOtp.equals(request.getOtp())) {
            throw new UnauthorizedException("Mã OTP không hợp lệ hoặc đã hết hạn");
        }

        // Find user
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("Người dùng không tồn tại"));

        // Update password
        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        // Delete OTP from Redis
        redisTemplate.delete(otpKey);

        log.info("Password reset successfully for user id: {}", user.getId());

        // Send password reset confirmation email (async, không block flow)
        try {
            emailService.sendPasswordResetEmail(user.getEmail(), user.getFullName());
        } catch (Exception e) {
            log.error("Failed to send password reset email to {}: {}",
                    user.getEmail(), e.getMessage());
            // Không throw exception
        }
    }

    @Transactional(readOnly = true)
    @Override
    public void verifyRegistrationOtp(VerifyRegistrationOtpRequest request) {
        log.info("Verifying registration OTP for email: {}", request.getEmail());

        String otpKey = REGISTER_OTP_PREFIX + request.getEmail();
        String storedOtp = redisTemplate.opsForValue().get(otpKey);

        if (storedOtp == null || !storedOtp.equals(request.getOtp())) {
            throw new UnauthorizedException("Mã OTP không hợp lệ hoặc đã hết hạn");
        }

        // Verify user exists
        if (!userRepository.existsByEmail(request.getEmail())) {
            throw new ResourceNotFoundException("Người dùng không tồn tại");
        }

        // Delete OTP from Redis (one-time use)
        redisTemplate.delete(otpKey);

        log.info("Registration OTP verified successfully for email: {}", request.getEmail());
    }
}
