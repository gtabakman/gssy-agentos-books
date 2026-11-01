package com.example.books.ui;

import com.example.books.api.dto.AuthorDto;
import com.example.books.api.dto.BookDto;
import com.example.books.api.dto.TagDto;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.combobox.MultiSelectComboBox;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.value.ValueChangeMode;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public class BookEditorForm extends FormLayout {

  private final TextField title = new TextField(Messages.get("book.title"));
  private final TextField subtitle = new TextField(Messages.get("book.subtitle"));
  private final MultiSelectComboBox<AuthorDto> authors = new MultiSelectComboBox<>();
  private final MultiSelectComboBox<TagDto> tags = new MultiSelectComboBox<>();
  private final TextField isbn = new TextField();
  private final TextField publisher = new TextField();
  private final IntegerField year = new IntegerField();
  private final TextArea notes = new TextArea(Messages.get("book.notes"));
  private final Checkbox wishlist = new Checkbox(Messages.get("book.wishlist"));

  public BookEditorForm(
      BookDto book,
      List<AuthorDto> availableAuthors,
      List<TagDto> availableTags,
      Runnable onAddAuthor,
      Runnable onAddTag,
      Runnable onLookupIsbn) {
    setResponsiveSteps(new ResponsiveStep("0", 1), new ResponsiveStep("30em", 2));
    setWidthFull();
    getStyle().set("max-width", "100%");
    getStyle().set("overflow-x", "hidden");
    getStyle().set("box-sizing", "border-box");

    title.setRequiredIndicatorVisible(true);
    title.setClearButtonVisible(true);
    title.setValueChangeMode(ValueChangeMode.EAGER);
    title.setErrorMessage(Messages.get("validation.required"));

    subtitle.setClearButtonVisible(true);
    subtitle.setValueChangeMode(ValueChangeMode.EAGER);

    authors.setItemLabelGenerator(a -> a != null ? Objects.toString(a.fullName(), "") : "");
    authors.setItems(availableAuthors != null ? availableAuthors : List.of());
    authors.setPlaceholder(Messages.get("book.authors"));
    authors.setWidthFull();
    authors.getStyle().set("min-width", "0");
    authors.setAutoExpand(MultiSelectComboBox.AutoExpandMode.BOTH);

    tags.setItemLabelGenerator(t -> t != null ? Objects.toString(t.name(), "") : "");
    tags.setItems(availableTags != null ? availableTags : List.of());
    tags.setPlaceholder(Messages.get("tags.title"));
    tags.setWidthFull();
    tags.getStyle().set("min-width", "0");
    tags.setAutoExpand(MultiSelectComboBox.AutoExpandMode.BOTH);

    isbn.setClearButtonVisible(true);
    isbn.setValueChangeMode(ValueChangeMode.EAGER);
    isbn.setWidthFull();

    publisher.setClearButtonVisible(true);
    publisher.setValueChangeMode(ValueChangeMode.EAGER);
    publisher.setWidthFull();

    year.setMin(0);
    year.setMax(9999);
    year.setStep(1);
    year.setClearButtonVisible(true);
    year.setWidthFull();

    notes.setClearButtonVisible(true);
    notes.setHeight("80px");
    notes.setWidthFull();

    HorizontalLayout authorsRow = new HorizontalLayout(authors, addButton(VaadinIcon.PLUS, Messages.get("action.new"), onAddAuthor));
    authorsRow.setWidthFull();
    authorsRow.setPadding(false);
    authorsRow.setAlignItems(Alignment.CENTER);
    authorsRow.setFlexGrow(1, authors);
    authorsRow.setFlexGrow(0, authorsRow.getComponentAt(1));
    authorsRow.getStyle().set("min-width", "0");

    HorizontalLayout tagsRow = new HorizontalLayout(tags, addButton(VaadinIcon.PLUS, Messages.get("action.new"), onAddTag));
    tagsRow.setWidthFull();
    tagsRow.setPadding(false);
    tagsRow.setAlignItems(Alignment.CENTER);
    tagsRow.setFlexGrow(1, tags);
    tagsRow.setFlexGrow(0, tagsRow.getComponentAt(1));
    tagsRow.getStyle().set("min-width", "0");

    Button lookupIsbn = addButton(VaadinIcon.SEARCH, Messages.get("openlibrary.lookup"), onLookupIsbn);
    HorizontalLayout isbnRow = new HorizontalLayout(isbn, lookupIsbn);
    isbnRow.setWidthFull();
    isbnRow.setPadding(false);
    isbnRow.setAlignItems(Alignment.CENTER);
    isbnRow.setFlexGrow(1, isbn);
    isbnRow.setFlexGrow(0, lookupIsbn);
    isbnRow.getStyle().set("min-width", "0");

    add(title, 2);
    add(subtitle, 2);
    add(notes, 2);
    var authorsItem = addFormItem(authorsRow, new Span(Messages.get("book.authors")));
    setColspan(authorsItem, 2);
    var tagsItem = addFormItem(tagsRow, new Span(Messages.get("tags.title")));
    setColspan(tagsItem, 2);
    addFormItem(isbnRow, new Span(Messages.get("book.isbn")));
    addFormItem(publisher, new Span(Messages.get("book.publisher")));
    addFormItem(year, new Span(Messages.get("book.year")));
    add(wishlist);

    if (book != null) {
      title.setValue(defaultIfNull(book.title()));
      subtitle.setValue(defaultIfNull(book.subtitle()));
      if (book.authorIds() != null && !book.authorIds().isEmpty() && availableAuthors != null) {
        Set<AuthorDto> selectedAuthors =
            availableAuthors.stream()
                .filter(a -> a != null && book.authorIds().contains(a.id()))
                .collect(Collectors.toSet());
        authors.setValue(selectedAuthors);
      }
      isbn.setValue(defaultIfNull(book.isbn()));
      publisher.setValue(defaultIfNull(book.publisher()));
      year.setValue(book.year());
      notes.setValue(defaultIfNull(book.notes()));
      wishlist.setValue(book.wishlist());
      if (book.tagIds() != null && !book.tagIds().isEmpty() && availableTags != null) {
        Set<TagDto> selected =
            availableTags.stream()
                .filter(t -> t != null && book.tagIds().contains(t.id()))
                .collect(Collectors.toSet());
        tags.setValue(selected);
      }
    }
  }

  private static Button addButton(VaadinIcon icon, String tooltip, Runnable action) {
    Button b = new Button(new Icon(icon));
    b.setTooltipText(tooltip);
    b.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
    b.addClickListener(e -> action.run());
    return b;
  }

  public TextField getTitle() {
    return title;
  }

  public TextField getSubtitle() {
    return subtitle;
  }

  public MultiSelectComboBox<AuthorDto> getAuthors() {
    return authors;
  }

  public TextField getIsbn() {
    return isbn;
  }

  public TextField getPublisher() {
    return publisher;
  }

  public IntegerField getYear() {
    return year;
  }

  public TextArea getNotes() {
    return notes;
  }

  public Checkbox getWishlist() {
    return wishlist;
  }

  public MultiSelectComboBox<TagDto> getTags() {
    return tags;
  }

  public void selectAuthors(List<AuthorDto> availableAuthors, Set<Long> selectedIds) {
    authors.setItems(availableAuthors != null ? availableAuthors : List.of());
    authors.setValue(
        availableAuthors == null
            ? Set.of()
            : availableAuthors.stream()
                .filter(author -> author != null && selectedIds.contains(author.id()))
                .collect(Collectors.toSet()));
  }

  public void selectTags(List<TagDto> availableTags, Set<Long> selectedIds) {
    tags.setItems(availableTags != null ? availableTags : List.of());
    tags.setValue(
        availableTags == null
            ? Set.of()
            : availableTags.stream()
                .filter(tag -> tag != null && selectedIds.contains(tag.id()))
                .collect(Collectors.toSet()));
  }

  public void refreshAuthorLookups(List<AuthorDto> availableAuthors) {
    Set<AuthorDto> current = authors.getValue();
    Set<Long> selectedIds =
        current != null
            ? current.stream().filter(a -> a != null && a.id() != null).map(AuthorDto::id).collect(Collectors.toSet())
            : Set.of();
    authors.setItems(availableAuthors != null ? availableAuthors : List.of());
    if (!selectedIds.isEmpty() && availableAuthors != null) {
      authors.setValue(
          availableAuthors.stream()
              .filter(a -> a != null && selectedIds.contains(a.id()))
              .collect(Collectors.toSet()));
    } else if (current != null && !current.isEmpty()) {
      authors.clear();
    }
  }

  public void refreshTagLookups(List<TagDto> availableTags) {
    Set<TagDto> current = tags.getValue();
    Set<Long> selectedIds =
        current != null
            ? current.stream().filter(t -> t != null && t.id() != null).map(TagDto::id).collect(Collectors.toSet())
            : Set.of();
    tags.setItems(availableTags != null ? availableTags : List.of());
    if (!selectedIds.isEmpty() && availableTags != null) {
      tags.setValue(
          availableTags.stream()
              .filter(t -> t != null && selectedIds.contains(t.id()))
              .collect(Collectors.toSet()));
    } else if (current != null && !current.isEmpty()) {
      tags.clear();
    }
  }

  public void reset(List<AuthorDto> availableAuthors, List<TagDto> availableTags) {
    title.clear();
    subtitle.clear();
    authors.clear();
    tags.clear();
    isbn.clear();
    publisher.clear();
    year.clear();
    notes.clear();
    wishlist.clear();
    clearAllInvalid();
    refreshAuthorLookups(availableAuthors);
    refreshTagLookups(availableTags);
  }

  public void clearAllInvalid() {
    title.setInvalid(false);
    subtitle.setInvalid(false);
    isbn.setInvalid(false);
    publisher.setInvalid(false);
    year.setInvalid(false);
    notes.setInvalid(false);
  }

  private static String defaultIfNull(String s) {
    return s != null ? s : "";
  }
}
