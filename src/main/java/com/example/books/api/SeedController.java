package com.example.books.api;

import com.example.books.service.SeedService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/seed")
public class SeedController {

  private final SeedService seedService;

  public SeedController(SeedService seedService) {
    this.seedService = seedService;
  }

  @PostMapping
  public ResponseEntity<SeedService.Result> seed() {
    return ResponseEntity.ok(seedService.seed());
  }
}
