package com.example.books.ui;

import com.example.books.api.dto.AuthorDto;
import com.example.books.api.dto.BookDto;
import com.example.books.api.dto.TagDto;
import com.example.books.service.TagService;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.value.ValueChangeMode;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

public class BooksPage extends VerticalLayout {

  private final TextField text = new TextField();
  private final ComboBox<TagDto> tag = new ComboBox<>();
  private final Checkbox wishlist = new Checkbox(Messages.get("books.wishlistFilter"));
  private final Button clear = new Button(Messages.get("action.clear"), new Icon(VaadinIcon.CLOSE_SMALL));

  private final Div booksCards = new Div();
  private final Grid<BookDto> grid = new Grid<>(BookDto.class, false);
  private BookDto selected;
  private List<BookDto> lastItems = List.of();

  private final Function<Long, String> displayNameForTagId;
  private final Function<Long, String> displayNameForAuthorId;
  private final AtomicLong coverUrlVersion = new AtomicLong(System.currentTimeMillis());
  private final Runnable refresh;
  private final Consumer<BookDto> onEditRow;
  private final Consumer<BookDto> onDeleteRow;

  public BooksPage(
      TagService tagService,
      Runnable onNew,
      Runnable onEdit,
      Runnable onDelete,
      Consumer<BookDto> onEditRow,
      Consumer<BookDto> onDeleteRow,
      Function<Long, String> displayNameForTagId,
      Function<Long, String> displayNameForAuthorId,
      Runnable refresh) {
    this.displayNameForTagId = displayNameForTagId;
    this.displayNameForAuthorId = displayNameForAuthorId;
    this.refresh = refresh;
    this.onEditRow = onEditRow;
    this.onDeleteRow = onDeleteRow;

    setSizeFull();
    setPadding(false);
    setSpacing(true);

    text.setPlaceholder(Messages.get("books.search"));
    text.setLabel(null);
    text.setClearButtonVisible(true);
    text.setValueChangeMode(ValueChangeMode.EAGER);

    tag.setPlaceholder(Messages.get("tags.title"));
    tag.setLabel(null);
    tag.setItems(tagService.list());
    tag.setItemLabelGenerator(t -> t != null ? Objects.toString(t.name(), "") : "");
    tag.setClearButtonVisible(true);

    clear.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
    clear.addClickListener(
        e -> {
          text.clear();
          tag.clear();
          wishlist.clear();
          this.refresh.run();
        });

    // Desktop order: text, tag, wishlist, clear (via CSS order)
    text.getElement().getStyle().set("order", "0");
    tag.getElement().getStyle().set("order", "1");
    wishlist.getElement().getStyle().set("order", "2");
    clear.getElement().getStyle().set("order", "3");

    HorizontalLayout filters = new HorizontalLayout(text, wishlist, tag, clear);
    filters.setWidthFull();
    filters.setPadding(false);
    filters.setSpacing(true);
    filters.getStyle().set("flex-wrap", "wrap");
    filters.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.CENTER);

    Button newBtn = new Button(Messages.get("books.new"), new Icon(VaadinIcon.PLUS), e -> onNew.run());
    Button editBtn = new Button(Messages.get("books.edit"), new Icon(VaadinIcon.EDIT), e -> onEdit.run());
    Button deleteBtn = new Button(Messages.get("books.delete"), new Icon(VaadinIcon.TRASH), e -> onDelete.run());
    editBtn.setEnabled(false);
    deleteBtn.setEnabled(false);

    HorizontalLayout toolbar = new HorizontalLayout(newBtn, editBtn, deleteBtn);
    toolbar.setPadding(false);
    toolbar.setSpacing(true);

    HorizontalLayout header = new HorizontalLayout(filters, toolbar);
    header.setWidthFull();
    header.setPadding(false);
    header.setSpacing(true);
    header.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.CENTER);
    header.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
    header.getStyle().set("flex-wrap", "wrap");
    header.expand(filters);

    configureGrid(onEditRow, onDeleteRow);
    grid.setSizeFull();

    booksCards.setWidthFull();
    booksCards.getStyle().set("display", "flex");
    booksCards.getStyle().set("flex-direction", "column");
    booksCards.getStyle().set("gap", "var(--lumo-space-s)");
    booksCards.getStyle().set("overflow-x", "hidden");
    booksCards.getStyle().set("overflow-y", "auto");
    booksCards.setVisible(false);

    grid.asSingleSelect()
        .addValueChangeListener(
            ev -> {
              boolean hasSelection = ev.getValue() != null;
              selected = ev.getValue();
              editBtn.setEnabled(hasSelection);
              deleteBtn.setEnabled(hasSelection);
              renderCards();
            });

    add(header, booksCards, grid);
    expand(booksCards);
    expand(grid);
  }

  public Grid<BookDto> getGrid() {
    return grid;
  }

  public void setItems(List<BookDto> items) {
    lastItems = items != null ? items : List.of();
    grid.setItems(lastItems);
    renderCards();
  }

  public void refreshTagLookups(List<TagDto> availableTags) {
    Long selectedId = tag.getValue() != null ? tag.getValue().id() : null;
    tag.setItems(availableTags != null ? availableTags : List.of());
    if (selectedId != null && availableTags != null) {
      availableTags.stream()
          .filter(t -> t != null && Objects.equals(t.id(), selectedId))
          .findFirst()
          .ifPresentOrElse(tag::setValue, tag::clear);
    } else {
      tag.clear();
    }
  }

  public void refreshAuthorLookups(List<AuthorDto> availableAuthors) {
    if (grid.getDataProvider() != null) {
      grid.getDataProvider().refreshAll();
    }
    renderCards();
  }

  public void refreshAll() {
    if (grid.getDataProvider() != null) {
      grid.getDataProvider().refreshAll();
    }
  }

  public void select(BookDto dto) {
    grid.select(dto);
  }

  public void deselectAll() {
    grid.deselectAll();
  }

  public void setGridVisible(boolean visible) {
    grid.setVisible(visible);
  }

  public void setMobileMode(boolean mobile) {
    if (mobile) {
      // Mobile: natural DOM order (text, wishlist, tag, clear)
      text.getElement().getStyle().remove("order");
      tag.getElement().getStyle().remove("order");
      wishlist.getElement().getStyle().remove("order");
      clear.getElement().getStyle().remove("order");
    } else {
      // Desktop: text, tag, wishlist, clear
      text.getElement().getStyle().set("order", "0");
      tag.getElement().getStyle().set("order", "1");
      wishlist.getElement().getStyle().set("order", "2");
      clear.getElement().getStyle().set("order", "3");
    }
  }

  public Div getBooksCards() {
    return booksCards;
  }

  public BookDto getSelected() {
    return selected;
  }

  public void setOnFiltersChanged(Runnable onFiltersChanged) {
    if (onFiltersChanged == null) {
      return;
    }
    text.addValueChangeListener(e -> onFiltersChanged.run());
    tag.addValueChangeListener(e -> onFiltersChanged.run());
    wishlist.addValueChangeListener(e -> onFiltersChanged.run());
  }

  public String getTextFilter() {
    return text.getValue() != null ? text.getValue().trim() : "";
  }

  public Boolean getWishlistFilter() {
    return wishlist.getValue() ? Boolean.TRUE : null;
  }

  public TagDto getTagFilter() {
    return tag.getValue();
  }

  public void bumpCoverUrlVersion() {
    coverUrlVersion.incrementAndGet();
    renderCards();
    refreshAll();
  }

  private void configureGrid(Consumer<BookDto> onEditRow, Consumer<BookDto> onDeleteRow) {
    grid.addColumn(BookDto::id)
        .setHeader("#")
        .setWidth("70px")
        .setFlexGrow(0)
        .setSortable(true);
    grid.addColumn(BookDto::title)
        .setHeader(Messages.get("book.title"))
        .setWidth("180px")
        .setFlexGrow(2)
        .setSortable(true);
    grid.addColumn(
            c -> {
              if (c == null || c.authorIds() == null || c.authorIds().isEmpty()) {
                return "";
              }
              return c.authorIds().stream()
                  .map(id -> displayNameForAuthorId != null ? displayNameForAuthorId.apply(id) : Objects.toString(id, ""))
                  .filter(s -> s != null && !s.isBlank())
                  .limit(3)
                  .collect(Collectors.joining(", "));
            })
        .setHeader(Messages.get("book.authors"))
        .setWidth("160px")
        .setFlexGrow(1)
        .setSortable(false);
    grid.addColumn(BookDto::isbn)
        .setHeader(Messages.get("book.isbn"))
        .setWidth("120px")
        .setFlexGrow(1)
        .setSortable(true);
    grid.addColumn(BookDto::publisher)
        .setHeader(Messages.get("book.publisher"))
        .setWidth("140px")
        .setFlexGrow(1)
        .setSortable(true);
    grid.addColumn(BookDto::year)
        .setHeader(Messages.get("book.year"))
        .setWidth("90px")
        .setFlexGrow(0)
        .setSortable(true);
    grid.addComponentColumn(this::renderWishlist)
        .setHeader("")
        .setWidth("40px")
        .setFlexGrow(0);
    grid.addColumn(
            c -> {
              if (c == null || c.tagIds() == null || c.tagIds().isEmpty()) {
                return "";
              }
              return c.tagIds().stream()
                  .map(id -> displayNameForTagId != null ? displayNameForTagId.apply(id) : Objects.toString(id, ""))
                  .filter(s -> s != null && !s.isBlank())
                  .limit(3)
                  .collect(Collectors.joining(", "));
            })
        .setHeader(Messages.get("tags.title"))
        .setWidth("250px")
        .setFlexGrow(1);
    grid.addComponentColumn(c -> renderCover(c, 40))
        .setHeader(Messages.get("book.cover"))
        .setWidth("56px")
        .setFlexGrow(0);
    grid.addComponentColumn(c -> rowActions(c, onEditRow, onDeleteRow))
        .setHeader("")
        .setWidth("100px")
        .setFlexGrow(0);

    grid.addItemDoubleClickListener(e -> {
      if (e.getItem() != null && onEditRow != null) {
        onEditRow.accept(e.getItem());
      }
    });
  }

  private void renderCards() {
    booksCards.removeAll();
    for (BookDto b : lastItems) {
      if (b == null) {
        continue;
      }

      Div card = new Div();
      card.setWidthFull();
      styleCard(card, selected != null && selected.id() != null && Objects.equals(selected.id(), b.id()));

      Div headerRow = new Div();
      headerRow.getStyle().set("display", "flex");
      headerRow.getStyle().set("flex-wrap", "wrap");
      headerRow.getStyle().set("align-items", "center");
      headerRow.getStyle().set("gap", "var(--lumo-space-m)");

      Component cover = renderCover(b, 56);

      Div textBox = new Div();
      textBox.getStyle().set("display", "flex");
      textBox.getStyle().set("flex-direction", "column");
      textBox.getStyle().set("min-width", "0");
      textBox.getStyle().set("flex", "1");

      Span title = new Span(Objects.toString(b.title(), "").trim());
      title.getStyle().set("font-weight", "600");
      title.getStyle().set("font-size", "var(--lumo-font-size-l)");
      title.getStyle().set("word-break", "break-word");
      textBox.add(title);

      String subtitleText = Objects.toString(b.subtitle(), "").trim();
      if (!subtitleText.isBlank()) {
        Span subtitleSpan = new Span(subtitleText);
        subtitleSpan.getStyle().set("font-size", "var(--lumo-font-size-s)");
        subtitleSpan.getStyle().set("color", "var(--lumo-secondary-text-color)");
        subtitleSpan.getStyle().set("font-style", "italic");
        subtitleSpan.getStyle().set("word-break", "break-word");
        textBox.add(subtitleSpan);
      }

      int authorCount = b.authorIds() != null ? b.authorIds().size() : 0;
      String authorsText =
          b.authorIds() != null
              ? b.authorIds().stream()
                  .map(id -> displayNameForAuthorId != null ? displayNameForAuthorId.apply(id) : Objects.toString(id, ""))
                  .filter(s -> s != null && !s.isBlank())
                  .limit(2)
                  .collect(Collectors.joining(", "))
              : "";
      if (authorCount > 2) {
        authorsText = authorsText + " +" + (authorCount - 2);
      }
      if (!authorsText.isBlank()) {
        Span authorLine = new Span(authorsText);
        authorLine.getStyle().set("color", "var(--lumo-secondary-text-color)");
        authorLine.getStyle().set("white-space", "nowrap");
        authorLine.getStyle().set("overflow", "hidden");
        authorLine.getStyle().set("text-overflow", "ellipsis");
        authorLine.getStyle().set("max-width", "100%");
        textBox.add(authorLine);
      }

      if (b.wishlist()) {
        Div wishBadge = badge(Messages.get("book.wishlist"), VaadinIcon.STAR);
        wishBadge.getStyle().set("align-self", "flex-start");
        textBox.add(wishBadge);
      }

      headerRow.add(cover, textBox);

      Div badges = new Div();
      badges.getStyle().set("display", "flex");
      badges.getStyle().set("gap", "var(--lumo-space-xs)");
      badges.getStyle().set("flex-wrap", "wrap");
      badges.getStyle().set("margin-top", "var(--lumo-space-s)");

      String isbn = Objects.toString(b.isbn(), "").trim();
      if (!isbn.isBlank()) {
        badges.add(badge(isbn, VaadinIcon.BARCODE));
      }

      String publisher = Objects.toString(b.publisher(), "").trim();
      if (!publisher.isBlank()) {
        badges.add(badge(publisher, VaadinIcon.BUILDING));
      }

      if (b.year() != null) {
        badges.add(badge(String.valueOf(b.year()), VaadinIcon.CALENDAR));
      }

      int tagCount = b.tagIds() != null ? b.tagIds().size() : 0;
      if (tagCount > 0) {
        String tagsText =
            b.tagIds().stream()
                .map(id -> displayNameForTagId != null ? displayNameForTagId.apply(id) : Objects.toString(id, ""))
                .filter(s -> s != null && !s.isBlank())
                .limit(2)
                .collect(Collectors.joining(", "));
        if (tagCount > 2) {
          tagsText = tagsText + " +" + (tagCount - 2);
        }
        badges.add(badge(tagsText, VaadinIcon.TAGS));
      }

      card.add(headerRow);
      if (badges.getComponentCount() > 0) {
        card.add(badges);
      }
      card.add(cardActions(b));

      card.addClickListener(ev -> select(b));
      booksCards.add(card);
    }
  }

  private Component renderWishlist(BookDto b) {
    if (b != null && b.wishlist()) {
      Icon star = new Icon(VaadinIcon.STAR);
      star.setSize("18px");
      star.getStyle().set("color", "var(--lumo-primary-color)");
      return star;
    }
    return new Span("");
  }

  private Component renderCover(BookDto b, int sizePx) {
    if (b == null || !b.hasCoverPicture() || b.id() == null) {
      return renderDimCover(sizePx);
    }

    Image img = new Image();
    img.setAlt(Messages.get("book.cover"));
    img.setWidth(sizePx + "px");
    img.setHeight((int) (sizePx * 1.4) + "px");
    img.getStyle().set("border-radius", "8px");
    img.getStyle().set("object-fit", "cover");
    img.setSrc("/api/books/" + b.id() + "/cover?v=" + coverUrlVersion.get());
    return img;
  }

  private static Div renderDimCover(int sizePx) {
    Div cover = new Div();
    cover.setWidth(sizePx + "px");
    cover.setHeight((int) (sizePx * 1.4) + "px");
    cover.getStyle().set("border-radius", "8px");
    cover.getStyle().set("box-sizing", "border-box");
    cover.getStyle().set("overflow", "hidden");
    cover.getStyle().set("background", "var(--lumo-contrast-5pct)");
    cover.getStyle().set("border", "1px solid var(--lumo-contrast-20pct)");
    cover.getStyle().set("display", "flex");
    cover.getStyle().set("align-items", "center");
    cover.getStyle().set("justify-content", "center");

    Icon icon = new Icon(VaadinIcon.BOOK);
    icon.setSize(Math.max(16, (int) (sizePx * 0.45)) + "px");
    icon.getStyle().set("color", "var(--lumo-secondary-text-color)");
    cover.add(icon);
    return cover;
  }

  private static void styleCard(Div card, boolean selected) {
    card.setWidthFull();
    card.getStyle().set("box-sizing", "border-box");
    card.getStyle().set("max-width", "100%");
    card.getStyle().set("border", "1px solid var(--lumo-contrast-20pct)");
    card.getStyle().set("border-radius", "var(--lumo-border-radius-l)");
    card.getStyle().set("padding", "var(--lumo-space-m)");
    card.getStyle().set("background", "var(--lumo-base-color)");
    card.getStyle().set("box-shadow", "var(--lumo-box-shadow-s)");
    card.getStyle().set("cursor", "pointer");
    if (selected) {
      card.getStyle().set("border", "1px solid var(--lumo-primary-color)");
      card.getStyle().set("background", "var(--lumo-primary-color-10pct)");
    }
  }

  private static Div badge(String text, VaadinIcon icon) {
    Div b = new Div();
    b.getStyle().set("display", "inline-flex");
    b.getStyle().set("align-items", "center");
    b.getStyle().set("gap", "0.25rem");
    b.getStyle().set("padding", "0.15rem 0.5rem");
    b.getStyle().set("border-radius", "999px");
    b.getStyle().set("background", "var(--lumo-contrast-5pct)");
    b.getStyle().set("color", "var(--lumo-body-text-color)");
    b.getStyle().set("font-size", "var(--lumo-font-size-xs)");
    b.getStyle().set("white-space", "nowrap");
    b.getStyle().set("max-width", "100%");

    Icon i = icon != null ? new Icon(icon) : null;
    if (i != null) {
      i.setSize("14px");
      i.getStyle().set("color", "var(--lumo-secondary-text-color)");
      b.add(i);
    }

    Span s = new Span(Objects.toString(text, "").trim());
    s.getStyle().set("overflow", "hidden");
    s.getStyle().set("text-overflow", "ellipsis");
    b.add(s);
    return b;
  }

  private static Component rowActions(
      BookDto book, Consumer<BookDto> onEditRow, Consumer<BookDto> onDeleteRow) {
    HorizontalLayout actions = new HorizontalLayout();
    actions.setPadding(false);
    actions.setSpacing(true);
    actions.setAlignItems(FlexComponent.Alignment.CENTER);

    Button edit = new Button(new Icon(VaadinIcon.EDIT));
    edit.setTooltipText(Messages.get("books.edit"));
    edit.addThemeVariants(ButtonVariant.LUMO_TERTIARY_INLINE);
    edit.addClickListener(e -> onEditRow.accept(book));

    Button delete = new Button(new Icon(VaadinIcon.TRASH));
    delete.setTooltipText(Messages.get("books.delete"));
    delete.addThemeVariants(ButtonVariant.LUMO_TERTIARY_INLINE, ButtonVariant.LUMO_ERROR);
    delete.addClickListener(e -> onDeleteRow.accept(book));

    actions.add(edit, delete);
    return actions;
  }

  private Component cardActions(BookDto book) {
    HorizontalLayout actions = new HorizontalLayout();
    actions.setPadding(false);
    actions.setSpacing(true);
    actions.setWidthFull();
    actions.setJustifyContentMode(FlexComponent.JustifyContentMode.END);
    actions.setAlignItems(FlexComponent.Alignment.CENTER);

    Button edit = new Button(new Icon(VaadinIcon.EDIT));
    edit.setTooltipText(Messages.get("books.edit"));
    edit.addThemeVariants(ButtonVariant.LUMO_TERTIARY_INLINE);
    edit.addClickListener(e -> onEditRow.accept(book));

    Button delete = new Button(new Icon(VaadinIcon.TRASH));
    delete.setTooltipText(Messages.get("books.delete"));
    delete.addThemeVariants(ButtonVariant.LUMO_TERTIARY_INLINE, ButtonVariant.LUMO_ERROR);
    delete.addClickListener(e -> onDeleteRow.accept(book));

    actions.add(edit, delete);
    return actions;
  }
}
