package com.example.books.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "books")
public class Book {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @NotBlank
  @Size(max = 255)
  @Column(nullable = false, length = 255)
  private String title;

  @Size(max = 255)
  @Column(length = 255)
  private String subtitle;

  @Size(max = 20)
  @Column(length = 20)
  private String isbn;

  @Size(max = 120)
  @Column(length = 120)
  private String publisher;

  @Column(name = "published_year")
  private Integer year;

  @Size(max = 2000)
  @Column(length = 2000)
  private String notes;

  @Column(nullable = false)
  private boolean wishlist;

  @Lob
  private byte[] coverPicture;

  @Size(max = 100)
  @Column(length = 100)
  private String coverPictureContentType;

  @ManyToMany(fetch = FetchType.LAZY)
  @JoinTable(
      name = "book_authors",
      joinColumns = @JoinColumn(name = "book_id"),
      inverseJoinColumns = @JoinColumn(name = "author_id"))
  private Set<Author> authors = new HashSet<>();

  @ManyToMany(fetch = FetchType.LAZY)
  @JoinTable(
      name = "book_tags",
      joinColumns = @JoinColumn(name = "book_id"),
      inverseJoinColumns = @JoinColumn(name = "tag_id"))
  private Set<Tag> tags = new HashSet<>();

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

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

  public byte[] getCoverPicture() {
    return coverPicture;
  }

  public void setCoverPicture(byte[] coverPicture) {
    this.coverPicture = coverPicture;
  }

  public String getCoverPictureContentType() {
    return coverPictureContentType;
  }

  public void setCoverPictureContentType(String coverPictureContentType) {
    this.coverPictureContentType = coverPictureContentType;
  }

  public Set<Author> getAuthors() {
    return authors;
  }

  public void setAuthors(Set<Author> authors) {
    this.authors = authors;
  }

  public Set<Tag> getTags() {
    return tags;
  }

  public void setTags(Set<Tag> tags) {
    this.tags = tags;
  }
}
