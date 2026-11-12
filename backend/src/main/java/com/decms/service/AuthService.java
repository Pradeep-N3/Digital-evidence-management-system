package com.decms.service;

import com.decms.config.JwtUtil;
import com.decms.domain.model.user.*;
import com.decms.dto.AuthDTO;
import com.decms.exception.BadRequestException;
import com.decms.repository.UserRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;
    private final AuditService auditService;

    public AuthService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       JwtUtil jwtUtil,
                       AuthenticationManager authenticationManager,
                       AuditService auditService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
        this.authenticationManager = authenticationManager;
        this.auditService = auditService;
    }

    @PostConstruct
    public void seedInitialUsers() {
        if (userRepository.count() == 0) {
            // Default Administrator
            Administrator admin = new Administrator(
                    "admin",
                    "admin@forensics.agency",
                    passwordEncoder.encode("admin123"),
                    "Chief Forensic Administrator",
                    "BADGE-ADM-01",
                    "Forensic Operations Directorate"
            );
            userRepository.save(admin);

            // Default Investigator
            Investigator inv = new Investigator(
                    "investigator",
                    "lead.investigator@forensics.agency",
                    passwordEncoder.encode("investigator123"),
                    "Detective Alex Carter",
                    "BADGE-INV-44",
                    "Cybercrime & Digital Forensics Unit"
            );
            userRepository.save(inv);

            // Default Auditor
            Auditor aud = new Auditor(
                    "auditor",
                    "lead.auditor@compliance.gov",
                    passwordEncoder.encode("auditor123"),
                    "Inspector Sarah Jenkins",
                    "BADGE-AUD-99",
                    "Independent Judiciary Oversight",
                    "CERT-ISO-27037-LEAD"
            );
            userRepository.save(aud);

            auditService.recordEvent(null, "SYSTEM_INIT", "User", "SYSTEM", "127.0.0.1", "SUCCESS", "Initial forensic roles seeded");
        }
    }

    public AuthDTO.AuthResponse login(AuthDTO.LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
        );

        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new BadRequestException("User record not found"));

        if (!user.isActive()) {
            auditService.recordEvent(user, "LOGIN_ATTEMPT", "User", user.getUsername(), "127.0.0.1", "FAILURE", "Inactive account login attempt");
            throw new BadRequestException("Account has been deactivated. Please contact an Administrator.");
        }

        String token = jwtUtil.generateToken(user);
        auditService.recordEvent(user, "LOGIN_SUCCESS", "User", user.getUsername(), "127.0.0.1", "SUCCESS", "User authenticated successfully");

        return new AuthDTO.AuthResponse(token, mapToUserDTO(user));
    }

    @Transactional
    public AuthDTO.UserDTO register(AuthDTO.RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new BadRequestException("Username is already in use: " + request.getUsername());
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BadRequestException("Email is already in use: " + request.getEmail());
        }

        User newUser;
        String encodedPassword = passwordEncoder.encode(request.getPassword());

        if (request.getRole() == Role.ROLE_ADMIN) {
            newUser = new Administrator(
                    request.getUsername(),
                    request.getEmail(),
                    encodedPassword,
                    request.getFullName(),
                    request.getBadgeNumber(),
                    request.getDepartment()
            );
        } else if (request.getRole() == Role.ROLE_AUDITOR) {
            newUser = new Auditor(
                    request.getUsername(),
                    request.getEmail(),
                    encodedPassword,
                    request.getFullName(),
                    request.getBadgeNumber(),
                    request.getDepartment(),
                    request.getCertificationNumber()
            );
        } else {
            newUser = new Investigator(
                    request.getUsername(),
                    request.getEmail(),
                    encodedPassword,
                    request.getFullName(),
                    request.getBadgeNumber(),
                    request.getDepartment()
            );
        }

        User savedUser = userRepository.save(newUser);
        auditService.recordEvent(savedUser, "USER_REGISTERED", "User", savedUser.getUsername(), "127.0.0.1", "SUCCESS", "New user registered with role " + savedUser.getRole());
        return mapToUserDTO(savedUser);
    }

    public User getCurrentlyAuthenticatedUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            throw new BadRequestException("No authenticated forensic session found");
        }
        return userRepository.findByUsername(auth.getName())
                .orElseThrow(() -> new BadRequestException("Authenticated user not found in database"));
    }

    public AuthDTO.UserDTO mapToUserDTO(User user) {
        AuthDTO.UserDTO dto = new AuthDTO.UserDTO();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setEmail(user.getEmail());
        dto.setFullName(user.getFullName());
        dto.setRole(user.getRole());
        dto.setBadgeNumber(user.getBadgeNumber());
        dto.setDepartment(user.getDepartment());
        dto.setActive(user.isActive());
        dto.setCreatedAt(user.getCreatedAt());
        return dto;
    }
}
