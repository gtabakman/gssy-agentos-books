package com.example.books.repo;

import com.example.books.domain.Author;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuthorRepository extends JpaRepository<Author, Long> {
  Optional<Author> findByFullName(String fullName);

  boolean existsByFullName(String fullName);
}
