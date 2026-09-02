package com.example.books.ui;

import com.example.books.api.dto.AuthorDto;
import com.example.books.api.dto.BookDto;
import com.example.books.api.dto.CreateBookRequest;
import com.example.books.api.dto.TagDto;
import com.example.books.api.dto.UpdateBookRequest;
import com.example.books.service.AuthorService;
import com.example.books.service.BookService;
import com.example.books.service.NotFoundException;
import com.example.books.service.TagService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.FlexComponent.JustifyContentMode;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

public class BookEditorDialog extends Dialog {

  private final BookEditorForm form;
  private final ImageUpload imageUpload;
  private final BookService bookService;
  private final TagService tagService;
  private final AuthorService authorService;
  private final BookDto book;
  private final Runnable afterSave;

  public BookEditorDialog(
      BookDto book,
      BookService bookService,
      TagService tagService,
      AuthorService authorService,
      Runnable afterSave,
      Consumer<BookDto> onDeleteRequested) {
    this.book = book;
    this.bookService = bookService;
    this.tagService = tagService;
    this.authorService = authorService;
    this.afterSave = afterSave;

    setHeaderTitle(book == null ? Messages.get("editor.newBook") : Messages.get("editor.editBook"));
    setCloseOnOutsideClick(false);
    setWidth("75vw");
    setMaxWidth("100vw");
    setMaxHeight("90vh");
    getElement().getStyle().set("box-sizing", "border-box");
    getElement().getStyle().set("overflow-x", "hidden");

    form = new BookEditorForm(book, authorService.list(), tagService.list(), this::openAuthorEditor, this::openTagEditor);

    imageUpload = new ImageUpload(Messages.get("book.cover"), 120);
    if (book != null && book.hasCoverPicture()) {
      try {
        BookService.CoverPicture pic = bookService.getCoverPicture(book.id());
        imageUpload.setImage(pic.bytes(), pic.contentType());
      } catch (NotFoundException ex) {
        imageUpload.setImage(null, null);
      }
    } else {
      imageUpload.setImage(null, null);
    }

    VerticalLayout content = new VerticalLayout(form, imageUpload);
    content.setPadding(false);
    content.setSpacing(true);
    content.setWidthFull();
    content.getStyle().set("overflow-x", "hidden");
    content.getStyle().set("max-width", "100%");
    add(content);

    Button cancel = new Button(Messages.get("dialog.cancel"), e -> close());
    Button save = new Button(Messages.get("dialog.save"), e -> save(false));
    save.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

    Button saveAndAnother = null;
    if (book == null) {
      saveAndAnother = new Button(Messages.get("dialog.saveAndAnother"), e -> save(true));
      saveAndAnother.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
    }

    Button delete = null;
    if (book != null) {
      delete =
          new Button(
              Messages.get("dialog.delete"),
              e -> {
                close();
                if (onDeleteRequested != null) {
                  onDeleteRequested.accept(book);
                }
              });
      delete.addThemeVariants(ButtonVariant.LUMO_ERROR);
    }

    HorizontalLayout startGroup = new HorizontalLayout();
    startGroup.setPadding(false);
    startGroup.setSpacing(true);
    if (delete != null) {
      startGroup.add(delete);
    }
    if (saveAndAnother != null) {
      startGroup.add(saveAndAnother);
    }

    HorizontalLayout endGroup = new HorizontalLayout(cancel, save);
    endGroup.setPadding(false);
    endGroup.setSpacing(true);

    HorizontalLayout footer = new HorizontalLayout(startGroup, endGroup);
    footer.setWidthFull();
    footer.setJustifyContentMode(JustifyContentMode.BETWEEN);
    getFooter().add(footer);
  }

  public void refreshLookups() {
    form.refreshAuthorLookups(authorService.list());
    form.refreshTagLookups(tagService.list());
  }

  public void setMobileMode(boolean mobile) {
    imageUpload.setMobileMode(mobile);
    setWidth(mobile ? "100vw" : "75vw");
  }

  private void openAuthorEditor() {
    new AuthorEditorDialog(
        Messages.get("authors.new"),
        null,
        authorService,
        () -> form.refreshAuthorLookups(authorService.list()))
        .open();
  }

  private void openTagEditor() {
    new NameEditorDialog(
        Messages.get("tags.new"),
        null,
        v -> tagService.create(v),
        () -> form.refreshTagLookups(tagService.list()),
        true)
        .open();
  }

  private void save(boolean andAnother) {
    form.clearAllInvalid();

    String titleValue = form.getTitle().getValue();
    boolean ok = true;
    if (titleValue == null || titleValue.isBlank()) {
      form.getTitle().setInvalid(true);
      form.getTitle().setErrorMessage(Messages.get("validation.required"));
      ok = false;
    } else if (titleValue.length() > 255) {
      form.getTitle().setInvalid(true);
      form.getTitle().setErrorMessage(Messages.get("validation.tooLong"));
      ok = false;
    }

    String subtitleValue = form.getSubtitle().getValue();
    if (subtitleValue != null && subtitleValue.length() > 255) {
      form.getSubtitle().setInvalid(true);
      form.getSubtitle().setErrorMessage(Messages.get("validation.tooLong"));
      ok = false;
    }

    String isbnValue = form.getIsbn().getValue();
    if (isbnValue != null && isbnValue.length() > 20) {
      form.getIsbn().setInvalid(true);
      form.getIsbn().setErrorMessage(Messages.get("validation.tooLong"));
      ok = false;
    }

    String publisherValue = form.getPublisher().getValue();
    if (publisherValue != null && publisherValue.length() > 120) {
      form.getPublisher().setInvalid(true);
      form.getPublisher().setErrorMessage(Messages.get("validation.tooLong"));
      ok = false;
    }

    Integer yearValue = form.getYear().getValue();
    if (yearValue != null && (yearValue < 0 || yearValue > 9999)) {
      form.getYear().setInvalid(true);
      form.getYear().setErrorMessage(Messages.get("validation.tooLong"));
      ok = false;
    }

    String notesValue = form.getNotes().getValue();
    if (notesValue != null && notesValue.length() > 2000) {
      form.getNotes().setInvalid(true);
      form.getNotes().setErrorMessage(Messages.get("validation.tooLong"));
      ok = false;
    }

    if (!ok) {
      Notification.show(Messages.get("validation.fixFields"));
      return;
    }

    CreateBookRequest req = new CreateBookRequest();
    req.setTitle(titleValue.trim());
    req.setSubtitle(subtitleValue != null && !subtitleValue.isBlank() ? subtitleValue.trim() : null);
    req.setIsbn(isbnValue != null && !isbnValue.isBlank() ? isbnValue.trim() : null);
    req.setPublisher(publisherValue != null && !publisherValue.isBlank() ? publisherValue.trim() : null);
    req.setYear(yearValue);
    req.setNotes(notesValue != null && !notesValue.isBlank() ? notesValue.trim() : null);
    req.setWishlist(form.getWishlist().getValue());
    req.setAuthorIds(
        form.getAuthors().getValue() != null
            ? form.getAuthors().getValue().stream().filter(Objects::nonNull).map(AuthorDto::id).toList()
            : List.of());
    req.setTagIds(
        form.getTags().getValue() != null
            ? form.getTags().getValue().stream().filter(Objects::nonNull).map(TagDto::id).toList()
            : List.of());

    try {
      BookDto saved;
      if (book == null) {
        saved = bookService.create(req);
      } else {
        saved = bookService.update(book.id(), toUpdateRequest(req));
      }

      byte[] uploaded = imageUpload.getUploadedBytes();
      if (uploaded != null && uploaded.length > 0) {
        bookService.setCoverPicture(saved.id(), uploaded, imageUpload.getUploadedContentType());
      } else if (imageUpload.isRemoveExistingRequested()) {
        bookService.deleteCoverPicture(saved.id());
      }

      if (afterSave != null) {
        afterSave.run();
      }

      if (andAnother && book == null) {
        form.reset(authorService.list(), tagService.list());
        imageUpload.setImage(null, null);
        Notification.show(Messages.get("dialog.savedAndAnother"));
      } else {
        close();
        Notification.show(Messages.get("dialog.save"));
      }
    } catch (Exception ex) {
      Notification.show("Error saving book");
    }
  }

  private static UpdateBookRequest toUpdateRequest(CreateBookRequest from) {
    UpdateBookRequest to = new UpdateBookRequest();
    to.setTitle(from.getTitle());
    to.setSubtitle(from.getSubtitle());
    to.setIsbn(from.getIsbn());
    to.setPublisher(from.getPublisher());
    to.setYear(from.getYear());
    to.setNotes(from.getNotes());
    to.setWishlist(from.isWishlist());
    to.setAuthorIds(from.getAuthorIds());
    to.setTagIds(from.getTagIds());
    return to;
  }
}
