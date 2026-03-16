package sp26.group3.computer.sba301_computershop.service.impl;

import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import sp26.group3.computer.sba301_computershop.dto.request.*;
import sp26.group3.computer.sba301_computershop.dto.response.AuthenticationResponse;
import sp26.group3.computer.sba301_computershop.dto.response.IntrospectResponse;
import sp26.group3.computer.sba301_computershop.entity.InvalidatedToken;
import sp26.group3.computer.sba301_computershop.entity.User;
import sp26.group3.computer.sba301_computershop.exception.AppException;
import sp26.group3.computer.sba301_computershop.exception.ErrorCode;
import sp26.group3.computer.sba301_computershop.repository.InvalidatedTokenRepository;
import sp26.group3.computer.sba301_computershop.repository.RoleRepository;
import sp26.group3.computer.sba301_computershop.repository.UserRepository;
import sp26.group3.computer.sba301_computershop.service.AuthenticationService;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.Map;
import sp26.group3.computer.sba301_computershop.entity.Role;

import java.text.ParseException;
import java.util.Date;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class AuthenticationServiceImpl implements AuthenticationService {

    UserRepository userRepository;
    PasswordEncoder passwordEncoder;
    InvalidatedTokenRepository invalidatedTokenRepository;
    RoleRepository roleRepository;

    @Value("${jwt.signer-key}")
    @NonFinal
    String SIGNER_KEY;

    @Value("${jwt.valid-duration}")
    @NonFinal
    long VALID_DURATION;

    @Value("${jwt.refreshable-duration}")
    @NonFinal
    long REFRESHABLE_DURATION;

    // ================= LOGIN =================
    @Override
    public AuthenticationResponse authenticated(AuthenticationRequest request) {

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new AppException(ErrorCode.UNAUTHENTICATED));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }

        String token = generateToken(user);

        return AuthenticationResponse.builder()
                .token(token)
                .authenticated(true)
                .build();
    }

    // ================= GOOGLE LOGIN =================
    @Override
    public AuthenticationResponse googleLogin(GoogleLoginRequest request) {
        RestTemplate restTemplate = new RestTemplate();
        String url;
        HttpEntity<String> entity;

        if (request.getToken().startsWith("ya29.")) {
            // Access Token
            url = "https://www.googleapis.com/oauth2/v3/userinfo";
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + request.getToken());
            entity = new HttpEntity<>("", headers);
        } else {
            // ID Token
            url = "https://oauth2.googleapis.com/tokeninfo?id_token=" + request.getToken();
            entity = new HttpEntity<>("");
        }

        try {
            ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.GET, entity, Map.class);
            Map<String, Object> payload = response.getBody();
            if (payload == null || !payload.containsKey("email")) {
                throw new AppException(ErrorCode.UNAUTHENTICATED);
            }

            String email = (String) payload.get("email");
            String defaultName = payload.containsKey("name") ? (String) payload.get("name") : email;

            User user = userRepository.findByEmail(email).orElse(null);
            if (user == null) {
                // Tạo user mới nếu chưa tồn tại
                Role userRole = roleRepository.findByName("USER")
                        .orElseThrow(() -> new AppException(ErrorCode.UNCATEGORIZED_EXCEPTION)); // Cần có role USER trong DB

                user = User.builder()
                        .email(email)
                        .username(email)
                        .password(passwordEncoder.encode(java.util.UUID.randomUUID().toString()))
                        .role(userRole)
                        .status("ACTIVE")
                        .createdAt(LocalDateTime.now())
                        .build();
                user = userRepository.save(user);
            }

            String token = generateToken(user);
            return AuthenticationResponse.builder()
                    .token(token)
                    .authenticated(true)
                    .build();

        } catch (Exception e) {
            log.error("Google verify token failed", e);
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }
    }

    // ================= GENERATE JWT =================
    private String generateToken(User user) {
        try {
            Date now = new Date();
            Date expiry = new Date(now.getTime() + VALID_DURATION * 1000);

            String jti = UUID.randomUUID().toString();

            JWTClaimsSet claimsSet = new JWTClaimsSet.Builder()
                    .subject(user.getEmail())
                    .claim("userId", user.getUserId())
                    .claim("scope", user.getRole().getName())
                    .issueTime(now)
                    .expirationTime(expiry)
                    .jwtID(jti)
                    .build();

            SignedJWT jwt = new SignedJWT(
                    new JWSHeader(JWSAlgorithm.HS512),
                    claimsSet
            );

            jwt.sign(new MACSigner(SIGNER_KEY.getBytes()));
            return jwt.serialize();

        } catch (JOSEException e) {
            log.error("Cannot generate token", e);
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }
    }


    // ================= INTROSPECT =================
    @Override
    public IntrospectResponse introspect(IntrospectRequest request)
            throws ParseException, JOSEException {

        SignedJWT jwt = SignedJWT.parse(request.getToken());

        // verify signature
        boolean signatureValid = jwt.verify(
                new MACVerifier(SIGNER_KEY.getBytes())
        );

        if (!signatureValid) {
            return IntrospectResponse.builder().valid(false).build();
        }

        Date expiry = jwt.getJWTClaimsSet().getExpirationTime();
        if (expiry.before(new Date())) {
            return IntrospectResponse.builder().valid(false).build();
        }

        String jti = jwt.getJWTClaimsSet().getJWTID();

        // 🔥 check revoked
        if (invalidatedTokenRepository.existsById(jti)) {
            return IntrospectResponse.builder().valid(false).build();
        }

        return IntrospectResponse.builder()
                .valid(true)
                .build();
    }

    // ================= LOGOUT =================
    @Override
    public void logout(LogoutRequest request)
            throws ParseException, JOSEException {

        SignedJWT jwt = SignedJWT.parse(request.getToken());

        String jti = jwt.getJWTClaimsSet().getJWTID();
        Date expiry = jwt.getJWTClaimsSet().getExpirationTime();

        // đã logout rồi → bỏ qua
        if (invalidatedTokenRepository.existsById(jti)) {
            return;
        }

        invalidatedTokenRepository.save(
                InvalidatedToken.builder()
                        .id(jti)
                        .expiryTime(expiry)
                        .build()
        );

        log.info("Logout success - token revoked: {}", jti);
    }

    // ================= REFRESH TOKEN =================
    @Override
    public AuthenticationResponse refreshToken(RefreshRequest request)
            throws ParseException, JOSEException {

        SignedJWT jwt = SignedJWT.parse(request.getToken());

        String jti = jwt.getJWTClaimsSet().getJWTID();
        Date issuedAt = jwt.getJWTClaimsSet().getIssueTime();
        Date expiry = jwt.getJWTClaimsSet().getExpirationTime();

        // ❌ token đã logout
        if (invalidatedTokenRepository.existsById(jti)) {
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }

        // ❌ quá hạn refresh
        long refreshDeadline =
                issuedAt.getTime() + REFRESHABLE_DURATION * 1000;

        if (System.currentTimeMillis() > refreshDeadline) {
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }

        String email = jwt.getJWTClaimsSet().getSubject();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new AppException(ErrorCode.UNAUTHENTICATED));

        // 🔥 revoke token cũ
        invalidatedTokenRepository.save(
                InvalidatedToken.builder()
                        .id(jti)
                        .expiryTime(expiry)
                        .build()
        );

        String newToken = generateToken(user);

        return AuthenticationResponse.builder()
                .token(newToken)
                .authenticated(true)
                .build();
    }
}
