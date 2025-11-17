package com.example.cvd.repository;

import com.example.cvd.entity.Posts;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;


@Repository
public interface PostsRepository extends JpaRepository<Posts, Long> {
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Transactional
    @Query("UPDATE Posts p SET p.likes = p.likes + 1 WHERE p.id = :id")
    int incrementLikes(@Param("id") Long id);
}
