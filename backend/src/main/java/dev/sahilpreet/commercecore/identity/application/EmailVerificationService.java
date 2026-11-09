package dev.sahilpreet.commercecore.identity.application;

import dev.sahilpreet.commercecore.identity.persistence.JdbcUserRepository;
import dev.sahilpreet.commercecore.identity.persistence.JdbcVerificationTokenRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class EmailVerificationService {

    private final VerificationTokenHasher tokenHasher;
    private final JdbcVerificationTokenRepository tokenRepository;
    private final JdbcUserRepository userRepository;

    public EmailVerificationService(
            VerificationTokenHasher tokenHasher,
            JdbcVerificationTokenRepository tokenRepository,
            JdbcUserRepository userRepository
    ) {
        this.tokenHasher = tokenHasher;
        this.tokenRepository = tokenRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public void verify(String rawToken) {
        String hashedToken = tokenHasher.hash(rawToken);

        Long userId = tokenRepository.consume(hashedToken).orElseThrow(InvalidTokenException::new);

        int noOfRowsAffected = userRepository.markEmailVerified(userId);

        if (noOfRowsAffected != 1) {
            throw new IllegalStateException("Email verification state transition failed");
        }
    }
}