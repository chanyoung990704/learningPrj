package org.example.demo.repository;

import org.example.demo.domain.PostCategory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PostCategoryRepository extends JpaRepository<PostCategory, Long> {
    Optional<PostCategory> findByName(String name);
}