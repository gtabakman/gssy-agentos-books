package com.example.books.service;

import com.example.books.api.dto.AuthorDto;
import com.example.books.api.dto.CreateAuthorRequest;
import com.example.books.api.dto.UpdateAuthorRequest;
import com.example.books.domain.Author;
import com.example.books.domain.Book;
import com.example.books.repo.AuthorRepository;
import com.example.books.repo.BookRepository;
import java.util.List;
import java.util.Objects;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthorService {

  public record Usage(long books) {}

  private final AuthorRepository authorRepository;
  private final BookRepository bookRepository;

  public AuthorService(AuthorRepository authorRepository, BookRepository bookRepository) {
    this.authorRepository = authorRepository;
    this.bookRepository = bookRepository;
  }

  public List<AuthorDto> list() {
    return authorRepository.findAll(Sort.by(Author::getFullName)).stream()
        .map(this::toDto)
        .toList();
  }

  public AuthorDto get(Long id) {
    return authorRepository.findById(id).map(this::toDto).orElseThrow(() -> new NotFoundException("Author not found"));
  }

  @Transactional
  public AuthorDto create(CreateAuthorRequest request) {
    Author author = new Author();
    author.setFirstName(request.getFirstName() != null ? request.getFirstName().trim() : null);
    author.setLastName(request.getLastName() != null ? request.getLastName().trim() : null);
    author.setFullName(buildFullName(author.getFirstName(), author.getLastName()));
    if (authorRepository.existsByFullName(author.getFullName())) {
      throw new IllegalArgumentException("Author already exists");
    }
    return toDto(authorRepository.save(author));
  }

  @Transactional
  public AuthorDto update(Long id, UpdateAuthorRequest request) {
    Author author = authorRepository.findById(id).orElseThrow(() -> new NotFoundException("Author not found"));
    String first = request.getFirstName() != null ? request.getFirstName().trim() : author.getFirstName();
    String last = request.getLastName() != null ? request.getLastName().trim() : author.getLastName();
    String fullName = buildFullName(first, last);
    if (!Objects.equals(fullName, author.getFullName()) && authorRepository.existsByFullName(fullName)) {
      throw new IllegalArgumentException("Author already exists");
    }
    author.setFirstName(first);
    author.setLastName(last);
    author.setFullName(fullName);
    return toDto(authorRepository.save(author));
  }

  @Transactional
  public void delete(Long id) {
    Author author = authorRepository.findById(id).orElseThrow(() -> new NotFoundException("Author not found"));
    List<Book> books = bookRepository.findByAuthorsId(id);
    for (Book book : books) {
      book.getAuthors().remove(author);
    }
    bookRepository.saveAll(books);
    authorRepository.delete(author);
  }

  public Usage getUsage(Long id) {
    return new Usage(bookRepository.countByAuthorsId(id));
  }

  private static String buildFullName(String firstName, String lastName) {
    String first = firstName != null ? firstName.trim() : "";
    String last = lastName != null ? lastName.trim() : "";
    return (first + " " + last).trim();
  }

  private AuthorDto toDto(Author author) {
    return new AuthorDto(author.getId(), author.getFirstName(), author.getLastName(), author.getFullName());
  }
}
