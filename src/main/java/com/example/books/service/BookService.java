package com.example.books.service;

import com.example.books.api.dto.BookDto;
import com.example.books.api.dto.CreateBookRequest;
import com.example.books.api.dto.UpdateBookRequest;
import com.example.books.domain.Author;
import com.example.books.domain.Book;
import com.example.books.domain.Tag;
import com.example.books.repo.AuthorRepository;
import com.example.books.repo.BookRepository;
import jakarta.transaction.Transactional;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

@Service
public class BookService {

  private final BookRepository bookRepository;
  private final AuthorRepository authorRepository;
  private final TagService tagService;

  public BookService(
      BookRepository bookRepository,
      AuthorRepository authorRepository,
      TagService tagService) {
    this.bookRepository = bookRepository;
    this.authorRepository = authorRepository;
    this.tagService = tagService;
  }

  @Transactional
  public List<BookDto> list() {
    return search(null, null, null);
  }

  @Transactional
  public List<BookDto> search(String text, Boolean wishlist, Long tagId) {
    String q = text != null ? text.trim() : null;
    if (q != null && q.isEmpty()) {
      q = null;
    }
    return bookRepository.search(q, wishlist, tagId).stream().map(BookService::toDto).toList();
  }

  @Transactional
  public BookDto getById(Long id) {
    return toDto(findBook(id));
  }

  @Transactional
  public BookDto create(CreateBookRequest request) {
    Book book = new Book();
    copyFromRequest(book, request);
    Book saved = bookRepository.save(book);
    return toDto(saved);
  }

  @Transactional
  public BookDto update(Long id, UpdateBookRequest request) {
    Book book = findBook(id);
    copyFromRequest(book, request);
    return toDto(book);
  }

  @Transactional
  public void delete(Long id) {
    if (!bookRepository.existsById(id)) {
      throw new NotFoundException("Book not found");
    }
    bookRepository.deleteById(id);
  }

  @Transactional
  public void setCoverPicture(Long id, byte[] bytes, String contentType) {
    Book book = findBook(id);
    book.setCoverPicture(bytes);
    book.setCoverPictureContentType(contentType);
  }

  @Transactional
  public CoverPicture getCoverPicture(Long id) {
    Book book = findBook(id);
    if (book.getCoverPicture() == null || book.getCoverPicture().length == 0) {
      throw new NotFoundException("Cover picture not found");
    }
    return new CoverPicture(book.getCoverPicture(), book.getCoverPictureContentType());
  }

  @Transactional
  public void deleteCoverPicture(Long id) {
    Book book = findBook(id);
    book.setCoverPicture(null);
    book.setCoverPictureContentType(null);
  }

  private Book findBook(Long id) {
    return bookRepository.findById(id).orElseThrow(() -> new NotFoundException("Book not found"));
  }

  private void copyFromRequest(Book book, CreateBookRequest request) {
    book.setTitle(request.getTitle().trim());
    book.setSubtitle(request.getSubtitle() != null ? request.getSubtitle().trim() : null);
    book.setIsbn(request.getIsbn() != null ? request.getIsbn().trim() : null);
    book.setPublisher(request.getPublisher() != null ? request.getPublisher().trim() : null);
    book.setYear(request.getYear());
    book.setNotes(request.getNotes() != null ? request.getNotes().trim() : null);
    book.setWishlist(request.isWishlist());

    Set<Author> authors = loadAuthors(request.getAuthorIds());
    book.getAuthors().clear();
    book.getAuthors().addAll(authors);

    Set<Tag> tags = tagService.loadEntities(request.getTagIds());
    book.getTags().clear();
    book.getTags().addAll(tags);
  }

  private void copyFromRequest(Book book, UpdateBookRequest request) {
    book.setTitle(request.getTitle().trim());
    book.setSubtitle(request.getSubtitle() != null ? request.getSubtitle().trim() : null);
    book.setIsbn(request.getIsbn() != null ? request.getIsbn().trim() : null);
    book.setPublisher(request.getPublisher() != null ? request.getPublisher().trim() : null);
    book.setYear(request.getYear());
    book.setNotes(request.getNotes() != null ? request.getNotes().trim() : null);
    book.setWishlist(request.isWishlist());

    Set<Author> authors = loadAuthors(request.getAuthorIds());
    book.getAuthors().clear();
    book.getAuthors().addAll(authors);

    Set<Tag> tags = tagService.loadEntities(request.getTagIds());
    book.getTags().clear();
    book.getTags().addAll(tags);
  }

  private Set<Author> loadAuthors(List<Long> ids) {
    if (ids == null || ids.isEmpty()) {
      return java.util.Collections.emptySet();
    }
    return authorRepository.findAllById(ids).stream().collect(Collectors.toSet());
  }

  private static BookDto toDto(Book book) {
    return new BookDto(
        book.getId(),
        book.getTitle(),
        book.getSubtitle(),
        book.getAuthors().stream().map(Author::getId).filter(Objects::nonNull).sorted().toList(),
        book.getIsbn(),
        book.getPublisher(),
        book.getYear(),
        book.getNotes(),
        book.isWishlist(),
        book.getTags().stream().map(Tag::getId).filter(Objects::nonNull).sorted().toList(),
        book.getCoverPicture() != null && book.getCoverPicture().length > 0);
  }

  public record CoverPicture(byte[] bytes, String contentType) {}
}
