package ma.ensa.ebankingver1.impl;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import jakarta.persistence.TypedQuery;
import ma.ensa.ebankingver1.model.User2FASession;
import ma.ensa.ebankingver1.repository.User2FASessionRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public class User2FASessionRepositoryImpl implements User2FASessionRepository {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    @Transactional
    public void save(User2FASession session) {
        try {
            if (session.getId() == null) {
                entityManager.persist(session);
            } else {
                entityManager.merge(session);
            }
        } catch (Exception e) {
            throw new RuntimeException("Error saving User2FASession: " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public User2FASession findByUsernameAndVerifiedFalse(String username) {
        try {
            TypedQuery<User2FASession> query = entityManager.createQuery(
                    "SELECT s FROM User2FASession s WHERE s.username = :username AND s.verified = false",
                    User2FASession.class
            );
            query.setParameter("username", username);
            List<User2FASession> results = query.getResultList();
            return results.isEmpty() ? null : results.get(0);
        } catch (Exception e) {
            throw new RuntimeException("Error finding User2FASession: " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional
    public void deleteByUsername(String username) {
        try {
            Query query = entityManager.createQuery("DELETE FROM User2FASession s WHERE s.username = :username");
            query.setParameter("username", username);
            query.executeUpdate();
        } catch (Exception e) {
            throw new RuntimeException("Error deleting User2FASession: " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional
    public void markAsVerified(String username) {
        try {
            Query query = entityManager.createQuery(
                    "UPDATE User2FASession s SET s.verified = true WHERE s.username = :username AND s.verified = false"
            );
            query.setParameter("username", username);
            query.executeUpdate();
        } catch (Exception e) {
            throw new RuntimeException("Error marking User2FASession as verified: " + e.getMessage(), e);
        }
    }
}



