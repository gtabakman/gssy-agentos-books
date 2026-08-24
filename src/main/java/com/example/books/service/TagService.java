package com.example.books.service;

import com.example.books.api.dto.TagDto;
import com.example.books.domain.Tag;
import com.example.books.repo.BookRepository;
import com.example.books.repo.TagRepository;
import jakarta.transaction.Transactional;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.springframework.stereotype.Service;

@Service
public class TagService {

  private final TagRepository tagRepository;

  private final BookRepository bookRepository;

  public TagService(TagRepository tagRepository, BookRepository bookRepository) {
    this.tagRepository = tagRepository;
    this.bookRepository = bookRepository;
  }

  @Transactional
  public List<TagDto> list() {
    return tagRepository.findAll().stream().map(TagService::toDto).toList();
  }

  @Transactional
  public TagDto create(String name) {
    Tag tag = new Tag();
    tag.setName(name.trim());
    return toDto(tagRepository.save(tag));
  }

  @Transactional
  public TagDto update(Long id, String name) {
    Tag tag = tagRepository.findById(id).orElseThrow(() -> new NotFoundException("Tag not found"));
    tag.setName(name.trim());
    return toDto(tag);
  }

  @Transactional
  public Usage getUsage(Long id) {
    if (id == null || !tagRepository.existsById(id)) {
      throw new NotFoundException("Tag not found");
    }
    return new Usage(bookRepository.countByTags_Id(id));
  }

  @Transactional
  public void delete(Long id) {
    if (!tagRepository.existsById(id)) {
      throw new NotFoundException("Tag not found");
    }
    bookRepository.deleteTagLinksByTagId(id);
    tagRepository.deleteById(id);
  }

  @Transactional
  public Set<Tag> loadEntities(List<Long> ids) {
    if (ids == null || ids.isEmpty()) {
      return Set.of();
    }
    List<Tag> found = tagRepository.findAllById(ids);
    if (found.size() != new HashSet<>(ids).size()) {
      throw new NotFoundException("One or more tags not found");
    }
    return new HashSet<>(found);
  }

  private static TagDto toDto(Tag tag) {
    return new TagDto(tag.getId(), tag.getName());
  }

  public record Usage(long books) {}
}
