package com.example.books.service;

import com.example.books.api.dto.OpenLibraryBookDto;
import tools.jackson.databind.JsonNode;
import java.util.List;
import java.util.Locale;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
public class OpenLibraryService {

  private final RestClient client =
      RestClient.builder()
          .baseUrl("https://openlibrary.org")
          .defaultHeader("User-Agent", "FamilyBookLibrary/1.1 (Open Library ISBN lookup)")
          .build();

  public OpenLibraryBookDto lookupByIsbn(String isbn) {
    String normalized = normalizeIsbn(isbn);
    JsonNode response =
        client
            .get()
            .uri(
                uriBuilder ->
                    uriBuilder
                        .path("/search.json")
                        .queryParam("isbn", normalized)
                        .queryParam("lang", "he")
                        .queryParam("limit", 1)
                        .queryParam(
                            "fields",
                            "key,title,subtitle,author_name,publisher,first_publish_year,subject,language,edition_key,cover_i,editions")
                        .build())
            .accept(MediaType.APPLICATION_JSON)
            .retrieve()
            .body(JsonNode.class);
    JsonNode docs = response != null ? response.path("docs") : null;
    if (docs == null || !docs.isArray() || docs.isEmpty()) {
      throw new NotFoundException("Book not found in Open Library");
    }
    JsonNode book = docs.get(0);
    JsonNode edition = preferredEdition(book);
    Integer coverId = integer(edition, "cover_i");
    if (coverId == null) {
      coverId = integer(book, "cover_i");
    }
    return new OpenLibraryBookDto(
        normalized,
        firstText(edition, book, "title"),
        firstText(edition, book, "subtitle"),
        strings(book, "author_name"),
        firstStrings(edition, book, "publisher"),
        firstInteger(edition, book, "publish_year", "first_publish_year"),
        strings(book, "subject"),
        firstStrings(edition, book, "language"),
        text(book, "key"),
        strings(book, "edition_key"),
        coverId != null ? "https://covers.openlibrary.org/b/id/" + coverId + "-L.jpg" : null);
  }

  public Cover downloadCover(String coverUrl) {
    if (coverUrl == null || !coverUrl.startsWith("https://covers.openlibrary.org/")) {
      throw new IllegalArgumentException("Invalid Open Library cover URL");
    }
    byte[] bytes = client.get().uri(coverUrl).retrieve().body(byte[].class);
    return new Cover(bytes, "image/jpeg");
  }

  private static String normalizeIsbn(String isbn) {
    String normalized = isbn == null ? "" : isbn.replaceAll("[-\\s]", "").toUpperCase(Locale.ROOT);
    if (!normalized.matches("(?:\\d{9}[\\dX]|\\d{13})")) {
      throw new IllegalArgumentException("ISBN must contain 10 or 13 digits");
    }
    return normalized;
  }

  private static JsonNode preferredEdition(JsonNode book) {
    JsonNode editions = book.path("editions").path("docs");
    if (!editions.isArray() || editions.isEmpty()) {
      return book;
    }
    for (JsonNode edition : editions) {
      List<String> languages = strings(edition, "language");
      if (languages.contains("heb") || languages.contains("he")) {
        return edition;
      }
    }
    return editions.get(0);
  }

  private static String firstText(JsonNode preferred, JsonNode fallback, String field) {
    String value = text(preferred, field);
    return value != null ? value : text(fallback, field);
  }

  private static List<String> firstStrings(JsonNode preferred, JsonNode fallback, String field) {
    List<String> values = strings(preferred, field);
    return values.isEmpty() ? strings(fallback, field) : values;
  }

  private static Integer firstInteger(
      JsonNode preferred, JsonNode fallback, String preferredField, String fallbackField) {
    Integer value = integer(preferred, preferredField);
    return value != null ? value : integer(fallback, fallbackField);
  }

  private static Integer integer(JsonNode node, String field) {
    JsonNode value = node.path(field);
    if (value.isIntegralNumber()) {
      return value.intValue();
    }
    if (value.isArray() && !value.isEmpty() && value.get(0).isIntegralNumber()) {
      return value.get(0).intValue();
    }
    return null;
  }

  private static String text(JsonNode node, String field) {
    JsonNode fieldValue = node.path(field);
    String value = fieldValue.isString() ? fieldValue.asString().trim() : "";
    return value.isEmpty() ? null : value;
  }

  private static List<String> strings(JsonNode node, String field) {
    JsonNode values = node.path(field);
    if (!values.isArray()) {
      return List.of();
    }
    return java.util.stream.StreamSupport.stream(values.spliterator(), false)
        .filter(JsonNode::isString)
        .map(value -> value.asString().trim())
        .filter(value -> !value.isEmpty())
        .distinct()
        .toList();
  }

  public record Cover(byte[] bytes, String contentType) {}
}
