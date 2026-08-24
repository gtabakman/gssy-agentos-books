package com.example.books.api.dto;

import java.util.List;

public record BookDto(
    Long id,
    String title,
    String subtitle,
    List<Long> authorIds,
    String isbn,
    String publisher,
    Integer year,
    String notes,
    boolean wishlist,
    List<Long> tagIds,
    boolean hasCoverPicture) {}
