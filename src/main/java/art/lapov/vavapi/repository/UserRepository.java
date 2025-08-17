package art.lapov.vavapi.repository;

import art.lapov.vavapi.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, String> {

    /**
     * Find user by email
     */
    Optional<User> findByEmail(String email);

    /**
     * Find users by role with pagination
     */
    Page<User> findByRole(String role, Pageable pageable);

    /**
     * Search users by email, first name or last name
     * Case-insensitive search
     */
    @Query("SELECT u FROM User u WHERE " +
            "LOWER(u.email) LIKE %:query% OR " +
            "LOWER(u.firstName) LIKE %:query% OR " +
            "LOWER(u.lastName) LIKE %:query%")
    Page<User> searchUsers(@Param("query") String query, Pageable pageable);

    /**
     * Find all non-deleted users
     */
    @Query("SELECT u FROM User u WHERE u.deleted = false OR u.deleted IS NULL")
    Page<User> findAllActive(Pageable pageable);

    /**
     * Check if user has active (non-completed/cancelled) reservations (as client)
     */
    @Query("SELECT COUNT(r) > 0 FROM Reservation r WHERE r.client.id = :userId " +
            "AND r.status NOT IN ('COMPLETED', 'CANCELLED', 'REJECTED')")
    boolean hasActiveReservations(@Param("userId") String userId);

    /**
     * Check if user has active reservations on their stations (as owner)
     */
    @Query("SELECT COUNT(r) > 0 FROM Reservation r " +
            "WHERE r.station.location.owner.id = :userId " +
            "AND r.status NOT IN ('COMPLETED', 'CANCELLED', 'REJECTED')")
    boolean hasActiveReservationsAsOwner(@Param("userId") String userId);


    /**
     * Count users by role
     */
    long countByRole(String role);
}