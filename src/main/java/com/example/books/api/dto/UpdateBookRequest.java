package com.example.books.api.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.ArrayList;
import java.util.List;

public class UpdateBookRequest {

  @NotBlank
  @Size(max = 255)
  private String title;

  @Size(max = 255)
  private String subtitle;

  @Size(max = 20)
  private String isbn;

  @Size(max = 120)
  private String publisher;

  private Integer year;

  @Size(max = 2000)
  private String notes;

  private boolean wishlist;

  @Valid
  private List<Long> authorIds = new ArrayList<>();

  @Valid
  private List<Long> tagIds = new ArrayList<>();

  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public String getSubtitle() {
    return subtitle;
  }

  public void setSubtitle(String subtitle) {
    this.subtitle = subtitle;
  }

  public String getIsbn() {
    return isbn;
  }

  public void setIsbn(String isbn) {
    this.isbn = isbn;
  }

  public String getPublisher() {
    return publisher;
  }

  public void setPublisher(String publisher) {
    this.publisher = publisher;
  }

  public Integer getYear() {
    return year;
  }

  public void setYear(Integer year) {
    this.year = year;
  }

  public String getNotes() {
    return notes;
  }

  public void setNotes(String notes) {
    this.notes = notes;
  }

  public boolean isWishlist() {
    return wishlist;
  }

  public void setWishlist(boolean wishlist) {
    this.wishlist = wishlist;
  }

  public List<Long> getAuthorIds() {
    return authorIds;
  }

  public void setAuthorIds(List<Long> authorIds) {
    this.authorIds = authorIds;
  }

  public List<Long> getTagIds() {
    return tagIds;
  }

  public void setTagIds(List<Long> tagIds) {
    this.tagIds = tagIds;
  }
}
