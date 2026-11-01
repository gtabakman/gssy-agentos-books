package com.example.books.api;

import com.example.books.api.dto.OpenLibraryBookDto;
import com.example.books.service.OpenLibraryService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/open-library")
public class OpenLibraryController {

  private final OpenLibraryService openLibraryService;

  public OpenLibraryController(OpenLibraryService openLibraryService) {
    this.openLibraryService = openLibraryService;
  }

  @GetMapping("/isbn/{isbn}")
  public OpenLibraryBookDto getByIsbn(@PathVariable String isbn) {
    return openLibraryService.lookupByIsbn(isbn);
  }
}
