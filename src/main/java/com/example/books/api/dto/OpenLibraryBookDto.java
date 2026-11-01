package com.example.books.api.dto;

import java.util.List;

public record OpenLibraryBookDto(
    String isbn,
    String title,
    String subtitle,
    List<String> authors,
    List<String> publishers,
    Integer publishYear,
    List<String> subjects,
    List<String> languages,
    String openLibraryKey,
    List<String> editionKeys,
    String coverUrl) {}
