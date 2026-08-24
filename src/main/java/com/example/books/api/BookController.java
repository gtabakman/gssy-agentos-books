package com.example.books.api;

import com.example.books.api.dto.BookDto;
import com.example.books.api.dto.CreateBookRequest;
import com.example.books.api.dto.UpdateBookRequest;
import com.example.books.service.BookService;
import jakarta.validation.Valid;
import java.io.ByteArrayInputStream;
import java.net.URLConnection;
import java.util.List;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/books")
public class BookController {

  private final BookService bookService;

  public BookController(BookService bookService) {
    this.bookService = bookService;
  }

  @GetMapping
  public List<BookDto> list(
      @RequestParam(name = "text", required = false) String text,
      @RequestParam(name = "wishlist", required = false) Boolean wishlist,
      @RequestParam(name = "tagId", required = false) Long tagId) {
    return bookService.search(text, wishlist, tagId);
  }

  @GetMapping("/{id}")
  public BookDto getById(@PathVariable Long id) {
    return bookService.getById(id);
  }

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public BookDto create(@Valid @RequestBody CreateBookRequest request) {
    return bookService.create(request);
  }

  @PutMapping("/{id}")
  public BookDto update(@PathVariable Long id, @Valid @RequestBody UpdateBookRequest request) {
    return bookService.update(id, request);
  }

  @DeleteMapping("/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void delete(@PathVariable Long id) {
    bookService.delete(id);
  }

  @PostMapping(path = "/{id}/cover", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void uploadCover(@PathVariable Long id, @RequestParam("file") MultipartFile file) throws Exception {
    bookService.setCoverPicture(id, file.getBytes(), file.getContentType());
  }

  @GetMapping(path = "/{id}/cover")
  public ResponseEntity<byte[]> downloadCover(@PathVariable Long id) {
    BookService.CoverPicture pic = bookService.getCoverPicture(id);
    MediaType mt = MediaType.APPLICATION_OCTET_STREAM;
    if (pic.contentType() != null && !pic.contentType().isBlank()) {
      mt = MediaType.parseMediaType(pic.contentType());
    } else {
      try {
        String guessed = URLConnection.guessContentTypeFromStream(new ByteArrayInputStream(pic.bytes()));
        if (guessed != null && !guessed.isBlank()) {
          mt = MediaType.parseMediaType(guessed);
        }
      } catch (Exception ignored) {
      }
    }
    return ResponseEntity.ok()
        .contentType(mt)
        .cacheControl(CacheControl.noStore())
        .header(HttpHeaders.PRAGMA, "no-cache")
        .header(HttpHeaders.EXPIRES, "0")
        .header(HttpHeaders.CONTENT_DISPOSITION, "inline")
        .body(pic.bytes());
  }

  @DeleteMapping(path = "/{id}/cover")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void deleteCover(@PathVariable Long id) {
    bookService.deleteCoverPicture(id);
  }
}
