package com.example.books.api;

import com.example.books.api.dto.CreateTagRequest;
import com.example.books.api.dto.TagDto;
import com.example.books.api.dto.UpdateTagRequest;
import com.example.books.service.TagService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/tags")
public class TagController {

  private final TagService tagService;

  public TagController(TagService tagService) {
    this.tagService = tagService;
  }

  @GetMapping
  public List<TagDto> list() {
    return tagService.list();
  }

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public TagDto create(@Valid @RequestBody CreateTagRequest request) {
    return tagService.create(request.getName());
  }

  @PutMapping("/{id}")
  public TagDto update(@PathVariable Long id, @Valid @RequestBody UpdateTagRequest request) {
    return tagService.update(id, request.getName());
  }

  @DeleteMapping("/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void delete(@PathVariable Long id) {
    tagService.delete(id);
  }

  @GetMapping("/{id}/usage")
  public TagService.Usage usage(@PathVariable Long id) {
    return tagService.getUsage(id);
  }
}
