package com.example.demo.user.Repository;

import java.util.List;
import java.util.Optional;

import org.locationtech.jts.geom.Point;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    @Query(value = "SELECT * FROM users WHERE email = :email", nativeQuery = true)
    Optional<User> findByEmail(@Param("email") String email);

    @Query(value = "SELECT * FROM users WHERE name = :name", nativeQuery = true) 
    Optional<User> findByName(@Param("name") String name);
    
    @Query(value = "SELECT * FROM users WHERE email = :identifier OR name = :identifier", nativeQuery = true)
    Optional<User> findByEmailOrName(@Param("identifier") String identifier);

    @Query("SELECT u FROM User u " +
            "JOIN u.preferences up " +
            "JOIN Preferences cp ON cp.user.id = :currentUserId " +
            "WHERE u.id != :currentUserId " +
            "AND up.age BETWEEN :minAge AND :maxAge " +
            "AND up.gender = cp.preferredGender " +
            "AND cp.gender = up.preferredGender " +
            "ORDER BY ST_Distance(u.location, :point) ASC")
    List<User> findCompatibleNearbyUsers(
            @Param("currentUserId") Long currentUserId,
            @Param("point") Point point,
            @Param("minAge") int minAge,
            @Param("maxAge") int maxAge);

    @Query("SELECT u FROM User u WHERE u.active = true")
    List<User> findActiveUsers(@Param("active") String active);

}
