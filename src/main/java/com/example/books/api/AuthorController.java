package com.example.books.api;

import com.example.books.api.dto.AuthorDto;
import com.example.books.api.dto.CreateAuthorRequest;
import com.example.books.api.dto.UpdateAuthorRequest;
import com.example.books.service.AuthorService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/authors")
public class AuthorController {

  private final AuthorService authorService;

  public AuthorController(AuthorService authorService) {
    this.authorService = authorService;
  }

  @GetMapping
  public List<AuthorDto> list() {
    return authorService.list();
  }

  @GetMapping("/{id}")
  public ResponseEntity<AuthorDto> get(@PathVariable Long id) {
    return ResponseEntity.ok(authorService.get(id));
  }

  @PostMapping
  public ResponseEntity<AuthorDto> create(@Valid @RequestBody CreateAuthorRequest request) {
    return ResponseEntity.ok(authorService.create(request));
  }

  @PutMapping("/{id}")
  public ResponseEntity<AuthorDto> update(
      @PathVariable Long id, @Valid @RequestBody UpdateAuthorRequest request) {
    return ResponseEntity.ok(authorService.update(id, request));
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> delete(@PathVariable Long id) {
    authorService.delete(id);
    return ResponseEntity.ok().build();
  }
}
