package ma.ensa.ebankingver1.repository;

import ma.ensa.ebankingver1.model.User2FASession;

public interface User2FASessionRepository {
    void save(User2FASession session);
    User2FASession findByUsernameAndVerifiedFalse(String username);
    void deleteByUsername(String username);
    void markAsVerified(String username);
}

