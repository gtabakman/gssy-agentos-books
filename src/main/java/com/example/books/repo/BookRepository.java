package com.example.books.repo;

import com.example.books.domain.Book;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface BookRepository extends JpaRepository<Book, Long> {

  @Query(
      "SELECT DISTINCT b FROM Book b "
          + "LEFT JOIN b.authors a "
          + "LEFT JOIN b.tags t "
          + "WHERE (:text IS NULL OR :text = '' OR "
          + "LOWER(b.title) LIKE LOWER(CONCAT('%', :text, '%')) OR "
          + "LOWER(b.subtitle) LIKE LOWER(CONCAT('%', :text, '%')) OR "
          + "LOWER(a.fullName) LIKE LOWER(CONCAT('%', :text, '%')) OR "
          + "b.isbn LIKE CONCAT('%', :text, '%')) "
          + "AND (:wishlist IS NULL OR b.wishlist = :wishlist) "
          + "AND (:tagId IS NULL OR t.id = :tagId)")
  List<Book> search(
      @Param("text") String text,
      @Param("wishlist") Boolean wishlist,
      @Param("tagId") Long tagId);

  long countByTags_Id(Long tagId);

  List<Book> findByAuthorsId(Long authorId);

  long countByAuthorsId(Long authorId);

  @Modifying
  @Query(value = "delete from book_tags where tag_id = :tagId", nativeQuery = true)
  void deleteTagLinksByTagId(@Param("tagId") Long tagId);
}
