package com.example.demo.like.Repository;

import com.example.demo.user.Repository.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface LikeRepository extends JpaRepository<Like, Long> {
    @Query(value = """
    SELECT u.*
    FROM likes l
    JOIN users u ON\s
        (l.user_id = :userId AND u.id = l.user_target_id)
     OR (l.user_target_id = :userId AND u.id = l.user_id)
    WHERE l.match = true
   \s""", nativeQuery = true)
    List<User> findMatchedUsers(@Param("userId") Long userId);

    @Query(value = "SELECT * FROM likes WHERE user_id = :targetId AND user_target_id = :userId", nativeQuery = true)
    Optional<Like> findReverseLike(@Param("userId") Long userId, @Param("targetId") Long targetId);
}
