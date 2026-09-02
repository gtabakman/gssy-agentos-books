package com.example.books.ui;

import com.example.books.api.dto.AuthorDto;
import com.example.books.api.dto.BookDto;
import com.example.books.api.dto.TagDto;
import com.example.books.service.AuthorService;
import com.example.books.service.BookService;
import com.example.books.service.TagService;
import com.vaadin.flow.component.Direction;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.dependency.StyleSheet;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.page.Page;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.theme.lumo.Lumo;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

@Route("")
@StyleSheet(Lumo.STYLESHEET)
public class MainView extends VerticalLayout {

  private static final int MOBILE_MAX_WIDTH_PX = 768;

  private final BookService bookService;
  private final TagService tagService;
  private final AuthorService authorService;

  private final BooksPage booksPage;
  private final AuthorsPage authorsPage;
  private final TagsPage tagsPage;

  private Map<Long, String> tagNameById = Map.of();
  private Map<Long, String> authorNameById = Map.of();

  private BookEditorDialog openBookEditor;

  private volatile boolean mobileMode;

  public MainView(BookService bookService, TagService tagService, AuthorService authorService) {
    this.bookService = bookService;
    this.tagService = tagService;
    this.authorService = authorService;

    UI.getCurrent().setDirection(Direction.RIGHT_TO_LEFT);
    UI.getCurrent().setLocale(Locale.forLanguageTag("he-IL"));

    setSizeFull();
    setPadding(true);
    setSpacing(true);

    this.booksPage = buildBooksPage();
    this.authorsPage = buildAuthorsPage();
    this.tagsPage = buildTagsPage();

    MainTabs mainTabs = new MainTabs(booksPage, authorsPage, tagsPage);
    add(mainTabs);
    expand(mainTabs);

    refreshTagCache();
    refreshAuthorCache();
    refreshBooks();
    refreshTags();
    refreshAuthors();

    Page page = UI.getCurrent().getPage();
    page.executeJs("return window.innerWidth || document.documentElement.clientWidth || document.body.clientWidth;")
        .then(Integer.class, this::updateViewportMode);
    page.addBrowserWindowResizeListener(e -> updateViewportMode(e.getWidth()));
  }

  private void updateViewportMode(int widthPx) {
    setMobileMode(widthPx > 0 && widthPx <= MOBILE_MAX_WIDTH_PX);
  }

  private void setMobileMode(boolean mobile) {
    if (this.mobileMode == mobile) {
      return;
    }
    this.mobileMode = mobile;
    booksPage.setMobileMode(mobile);
    booksPage.getBooksCards().setVisible(mobile);
    booksPage.setGridVisible(!mobile);
    authorsPage.getCards().setVisible(mobile);
    authorsPage.setGridVisible(!mobile);
    tagsPage.getCards().setVisible(mobile);
    tagsPage.setGridVisible(!mobile);
  }

  private void refreshTagCache() {
    List<TagDto> all = tagService.list();
    this.tagNameById =
        all.stream()
            .filter(t -> t != null && t.id() != null)
            .collect(
                java.util.stream.Collectors.toUnmodifiableMap(
                    TagDto::id, t -> Objects.toString(t.name(), ""), (a, b) -> a));
  }

  private void refreshAuthorCache() {
    List<AuthorDto> all = authorService.list();
    this.authorNameById =
        all.stream()
            .filter(a -> a != null && a.id() != null)
            .collect(
                java.util.stream.Collectors.toUnmodifiableMap(
                    AuthorDto::id, a -> Objects.toString(a.fullName(), ""), (a, b) -> a));
  }

  private String displayNameForTagId(Long id) {
    if (id == null) {
      return "";
    }
    return Objects.toString(tagNameById.get(id), String.valueOf(id));
  }

  private String displayNameForAuthorId(Long id) {
    if (id == null) {
      return "";
    }
    return Objects.toString(authorNameById.get(id), String.valueOf(id));
  }

  private BooksPage buildBooksPage() {
    BooksPage page =
        new BooksPage(
            tagService,
            () -> openEditor(null),
            () -> openEditor(booksPage.getSelected()),
            () -> {
              BookDto sel = booksPage.getSelected();
              if (sel == null) {
                return;
              }
              confirmDelete(sel);
            },
            this::openEditor,
            this::confirmDelete,
            this::displayNameForTagId,
            this::displayNameForAuthorId,
            this::refreshBooks);

    page.setOnFiltersChanged(() -> applyBooksFilter(page));
    return page;
  }

  private AuthorsPage buildAuthorsPage() {
    AuthorsPage page =
        new AuthorsPage(
            authorService,
            () -> openAuthorEditor(null),
            () -> openAuthorEditor(authorsPage.getSelected()),
            () -> {
              AuthorDto sel = authorsPage.getSelected();
              if (sel == null) {
                return;
              }
              confirmDeleteAuthor(sel);
            },
            this::openAuthorEditor,
            this::confirmDeleteAuthor,
            this::refreshAuthors);

    page.setOnFiltersChanged(page::refresh);
    return page;
  }

  private TagsPage buildTagsPage() {
    TagsPage page =
        new TagsPage(
            tagService,
            () -> openTagEditor(null),
            () -> openTagEditor(tagsPage.getSelected()),
            () -> {
              TagDto sel = tagsPage.getSelected();
              if (sel == null) {
                return;
              }
              confirmDeleteTag(sel);
            },
            this::openTagEditor,
            this::confirmDeleteTag,
            this::refreshTags);

    page.setOnFiltersChanged(page::refresh);
    return page;
  }

  private void applyBooksFilter(BooksPage page) {
    if (page == null) {
      return;
    }
    try {
      refreshTagCache();
      refreshAuthorCache();
      String text = page.getTextFilter();
      Boolean wishlist = page.getWishlistFilter();
      Long tagId = page.getTagFilter() != null ? page.getTagFilter().id() : null;
      page.setItems(bookService.search(text, wishlist, tagId));
    } catch (Exception ex) {
      Notification.show("Error filtering books");
    }
  }

  private void refreshBooks() {
    applyBooksFilter(booksPage);
    booksPage.refreshAll();
    booksPage.refreshTagLookups(tagService.list());
    booksPage.refreshAuthorLookups(authorService.list());
  }

  private void refreshTags() {
    tagsPage.refresh();
    refreshTagCache();
    tagsPage.refreshAll();
    booksPage.refreshTagLookups(tagService.list());
    if (openBookEditor != null && openBookEditor.isOpened()) {
      openBookEditor.refreshLookups();
    }
  }

  private void refreshAuthors() {
    authorsPage.refresh();
    refreshAuthorCache();
    authorsPage.refreshAll();
    booksPage.refreshAuthorLookups(authorService.list());
    booksPage.refreshAll();
    if (openBookEditor != null && openBookEditor.isOpened()) {
      openBookEditor.refreshLookups();
    }
  }

  private void openEditor(BookDto book) {
    BookEditorDialog d =
        new BookEditorDialog(
            book,
            bookService,
            tagService,
            authorService,
            () -> {
              refreshBooks();
              refreshTags();
              refreshAuthors();
            },
            this::confirmDelete);
    openBookEditor = d;
    d.setMobileMode(mobileMode);
    d.addDetachListener(
        ev -> {
          if (openBookEditor == d) {
            openBookEditor = null;
          }
        });
    d.open();
  }

  private void confirmDelete(BookDto book) {
    if (book == null || book.id() == null) {
      return;
    }
    new ConfirmDeleteDialog(
            Messages.get("books.delete.confirm", book.title() != null ? book.title() : ""),
            () -> {
              bookService.delete(book.id());
              refreshBooks();
              refreshTags();
              refreshAuthors();
              BookDto selected = booksPage.getSelected();
              if (selected != null && Objects.equals(selected.id(), book.id())) {
                booksPage.deselectAll();
              }
            })
        .open();
  }

  private void openAuthorEditor(AuthorDto author) {
    AuthorEditorDialog d =
        new AuthorEditorDialog(
            author == null ? Messages.get("authors.new") : Messages.get("authors.edit"),
            author,
            authorService,
            () -> {
              refreshAuthors();
              refreshBooks();
            });
    d.open();
  }

  private void confirmDeleteAuthor(AuthorDto author) {
    if (author == null || author.id() == null) {
      return;
    }
    AuthorService.Usage usage = authorService.getUsage(author.id());
    String msg =
        Messages.get("authors.delete") + "?\n" + Messages.get("authors.usage", usage.books());
    new ConfirmDeleteDialog(
            msg,
            () -> {
              authorService.delete(author.id());
              refreshAuthors();
              refreshBooks();
              AuthorDto selected = authorsPage.getSelected();
              if (selected != null && Objects.equals(selected.id(), author.id())) {
                authorsPage.deselectAll();
              }
            })
        .open();
  }

  private void openTagEditor(TagDto tag) {
    NameEditorDialog d =
        new NameEditorDialog(
            tag == null ? Messages.get("tags.new") : Messages.get("tags.edit"),
            tag != null ? tag.name() : null,
            v -> {
              if (tag == null) {
                tagService.create(v);
              } else {
                tagService.update(tag.id(), v);
              }
            },
            this::refreshTags,
            tag == null);
    d.open();
  }

  private void confirmDeleteTag(TagDto tag) {
    if (tag == null || tag.id() == null) {
      return;
    }
    TagService.Usage usage = tagService.getUsage(tag.id());
    String msg =
        Messages.get("tags.delete") + "?\n" + Messages.get("tags.usage", usage.books());
    new ConfirmDeleteDialog(
            msg,
            () -> {
              tagService.delete(tag.id());
              refreshTags();
              refreshBooks();
              TagDto selected = tagsPage.getSelected();
              if (selected != null && Objects.equals(selected.id(), tag.id())) {
                tagsPage.deselectAll();
              }
            })
        .open();
  }
}
