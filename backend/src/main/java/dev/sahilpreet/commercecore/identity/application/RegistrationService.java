package dev.sahilpreet.commercecore.identity.application;

import dev.sahilpreet.commercecore.identity.persistence.JdbcUserRepository;
import dev.sahilpreet.commercecore.identity.persistence.JdbcVerificationTokenRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Locale;
import java.util.Optional;

@Service
public class RegistrationService {

    private static final int TTL = 30;

    private final VerificationTokenGenerator tokenGenerator;
    private final VerificationTokenHasher tokenHasher;
    private final PasswordEncoder passwordEncoder;
    private final JdbcUserRepository userRepository;
    private final JdbcVerificationTokenRepository tokenRepository;

    public RegistrationService(
            VerificationTokenGenerator tokenGenerator,
            VerificationTokenHasher tokenHasher,
            PasswordEncoder passwordEncoder,
            JdbcUserRepository userRepository,
            JdbcVerificationTokenRepository tokenRepository
    ) {
        this.tokenGenerator = tokenGenerator;
        this.tokenHasher = tokenHasher;
        this.passwordEncoder = passwordEncoder;
        this.userRepository = userRepository;
        this.tokenRepository = tokenRepository;
    }

    @Transactional
    public void register(String email, String password) {
        String normalizedEmail = email.trim().toLowerCase(Locale.ROOT);
        String hashedPassword = passwordEncoder.encode(password);

        Optional<Long> registeredUser = userRepository.registerUser(normalizedEmail, hashedPassword);

        if (registeredUser.isPresent()) {
            Long userId = registeredUser.get();
            String token = tokenGenerator.generate();
            String hashedToken = tokenHasher.hash(token);

            tokenRepository.issue(userId, hashedToken, OffsetDateTime.now(ZoneOffset.UTC).plusMinutes(TTL));
        }
    }
}
