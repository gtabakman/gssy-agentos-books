package com.example.books.ui;

import com.example.books.api.dto.TagDto;
import com.example.books.service.TagService;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.value.ValueChangeMode;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.function.Consumer;

public class TagsPage extends VerticalLayout {

  private final TextField nameFilter = new TextField();
  private final Button clear = new Button(Messages.get("action.clear"), new Icon(VaadinIcon.CLOSE_SMALL));

  private final Div tagCards = new Div();
  private final Grid<TagRow> grid = new Grid<>(TagRow.class, false);
  private TagRow selected;
  private List<TagRow> lastItems = List.of();

  private final TagService tagService;
  private final Runnable refresh;
  private final Consumer<TagDto> onEditRow;
  private final Consumer<TagDto> onDeleteRow;

  public TagsPage(
      TagService tagService,
      Runnable onNew,
      Runnable onEdit,
      Runnable onDelete,
      Consumer<TagDto> onEditRow,
      Consumer<TagDto> onDeleteRow,
      Runnable refresh) {
    this.tagService = tagService;
    this.refresh = refresh;
    this.onEditRow = onEditRow;
    this.onDeleteRow = onDeleteRow;

    setSizeFull();
    setPadding(false);
    setSpacing(true);

    nameFilter.setPlaceholder(Messages.get("books.search"));
    nameFilter.setLabel(null);
    nameFilter.setClearButtonVisible(true);
    nameFilter.setValueChangeMode(ValueChangeMode.EAGER);

    clear.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
    clear.addClickListener(
        e -> {
          nameFilter.clear();
          this.refresh.run();
        });

    HorizontalLayout filters = new HorizontalLayout(nameFilter, clear);
    filters.setWidthFull();
    filters.setPadding(false);
    filters.setSpacing(true);
    filters.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.END);
    filters.getStyle().set("flex-wrap", "wrap");
    filters.setFlexGrow(1, nameFilter);
    filters.setFlexGrow(0, clear);

    Button newBtn = new Button(Messages.get("action.new"), new Icon(VaadinIcon.PLUS), e -> onNew.run());
    Button editBtn = new Button(Messages.get("action.edit"), new Icon(VaadinIcon.EDIT), e -> onEdit.run());
    Button deleteBtn = new Button(Messages.get("action.delete"), new Icon(VaadinIcon.TRASH), e -> onDelete.run());
    editBtn.setEnabled(false);
    deleteBtn.setEnabled(false);

    HorizontalLayout toolbar = new HorizontalLayout(newBtn, editBtn, deleteBtn);
    toolbar.setPadding(false);
    toolbar.setSpacing(true);

    HorizontalLayout header = new HorizontalLayout(filters, toolbar);
    header.setWidthFull();
    header.setPadding(false);
    header.setSpacing(true);
    header.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.END);
    header.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
    header.getStyle().set("flex-wrap", "wrap");
    header.expand(filters);

    configureGrid(onEditRow, onDeleteRow);
    grid.setSizeFull();

    tagCards.setWidthFull();
    tagCards.getStyle().set("display", "flex");
    tagCards.getStyle().set("flex-direction", "column");
    tagCards.getStyle().set("gap", "var(--lumo-space-s)");
    tagCards.getStyle().set("overflow-x", "hidden");
    tagCards.getStyle().set("overflow-y", "auto");
    tagCards.setVisible(false);

    grid.asSingleSelect()
        .addValueChangeListener(
            ev -> {
              boolean hasSelection = ev.getValue() != null;
              selected = ev.getValue();
              editBtn.setEnabled(hasSelection);
              deleteBtn.setEnabled(hasSelection);
              renderCards();
            });

    add(header, tagCards, grid);
    expand(tagCards);
    expand(grid);
  }

  public Grid<TagRow> getGrid() {
    return grid;
  }

  public void setItems(List<TagRow> items) {
    lastItems = items != null ? items : List.of();
    grid.setItems(lastItems);
    renderCards();
  }

  public void refresh() {
    List<TagDto> tags = tagService.list();
    List<TagRow> rows =
        tags.stream()
            .map(t -> new TagRow(t, t.id() != null ? tagService.getUsage(t.id()).books() : 0))
            .toList();
    String q = normalize(nameFilter.getValue());
    if (!q.isBlank()) {
      rows =
          rows.stream()
              .filter(r -> r != null && r.tag() != null && normalize(r.tag().name()).contains(q))
              .toList();
    }
    setItems(rows);
  }

  public void refreshAll() {
    if (grid.getDataProvider() != null) {
      grid.getDataProvider().refreshAll();
    }
  }

  public TagDto getSelected() {
    return selected != null ? selected.tag() : null;
  }

  public void deselectAll() {
    grid.deselectAll();
  }

  public void setGridVisible(boolean visible) {
    grid.setVisible(visible);
  }

  public Div getCards() {
    return tagCards;
  }

  public void setOnFiltersChanged(Runnable onFiltersChanged) {
    if (onFiltersChanged == null) {
      return;
    }
    nameFilter.addValueChangeListener(e -> onFiltersChanged.run());
  }

  private void configureGrid(Consumer<TagDto> onEditRow, Consumer<TagDto> onDeleteRow) {
    grid.addColumn(r -> r.tag().id())
        .setHeader("#")
        .setWidth("70px")
        .setFlexGrow(0)
        .setSortable(true);
    grid.addColumn(r -> r.tag().name())
        .setHeader(Messages.get("tag.name"))
        .setWidth("180px")
        .setFlexGrow(2)
        .setSortable(true);
    grid.addColumn(r -> Messages.get("tags.usage", r.usage()))
        .setHeader(Messages.get("tags.title"))
        .setFlexGrow(1);
    grid.addComponentColumn(r -> rowActions(r.tag(), onEditRow, onDeleteRow))
        .setHeader("")
        .setWidth("100px")
        .setFlexGrow(0);

    grid.addItemDoubleClickListener(e -> {
      if (e.getItem() != null && e.getItem().tag() != null && onEditRow != null) {
        onEditRow.accept(e.getItem().tag());
      }
    });
  }

  private void renderCards() {
    tagCards.removeAll();
    for (TagRow r : lastItems) {
      if (r == null || r.tag() == null) {
        continue;
      }

      Div card = new Div();
      card.setWidthFull();
      styleCard(card, selected != null && selected.tag() != null && r.tag() != null && Objects.equals(selected.tag().id(), r.tag().id()));

      Div topRow = new Div();
      topRow.getStyle().set("display", "flex");
      topRow.getStyle().set("align-items", "center");
      topRow.getStyle().set("justify-content", "space-between");
      topRow.getStyle().set("gap", "var(--lumo-space-s)");

      Span name = new Span(Objects.toString(r.tag().name(), ""));
      name.getStyle().set("font-weight", "600");
      name.getStyle().set("word-break", "break-word");
      name.getStyle().set("min-width", "0");
      name.getStyle().set("flex", "1");

      topRow.add(name, cardActions(r.tag()));

      Span usage = new Span(Messages.get("tags.usage", r.usage()));
      usage.getStyle().set("font-size", "var(--lumo-font-size-xs)");
      usage.getStyle().set("color", "var(--lumo-secondary-text-color)");

      card.add(topRow, usage);
      card.addClickListener(ev -> grid.select(r));
      tagCards.add(card);
    }
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

  private static Component rowActions(
      TagDto tag, Consumer<TagDto> onEditRow, Consumer<TagDto> onDeleteRow) {
    HorizontalLayout actions = new HorizontalLayout();
    actions.setPadding(false);
    actions.setSpacing(true);
    actions.setAlignItems(FlexComponent.Alignment.CENTER);

    Button edit = new Button(new Icon(VaadinIcon.EDIT));
    edit.setTooltipText(Messages.get("action.edit"));
    edit.addThemeVariants(ButtonVariant.LUMO_TERTIARY_INLINE);
    edit.addClickListener(e -> onEditRow.accept(tag));

    Button delete = new Button(new Icon(VaadinIcon.TRASH));
    delete.setTooltipText(Messages.get("action.delete"));
    delete.addThemeVariants(ButtonVariant.LUMO_TERTIARY_INLINE, ButtonVariant.LUMO_ERROR);
    delete.addClickListener(e -> onDeleteRow.accept(tag));

    actions.add(edit, delete);
    return actions;
  }

  private Component cardActions(TagDto tag) {
    HorizontalLayout actions = new HorizontalLayout();
    actions.setPadding(false);
    actions.setSpacing(true);
    actions.setAlignItems(FlexComponent.Alignment.CENTER);

    Button edit = new Button(new Icon(VaadinIcon.EDIT));
    edit.setTooltipText(Messages.get("action.edit"));
    edit.addThemeVariants(ButtonVariant.LUMO_TERTIARY_INLINE);
    edit.addClickListener(e -> onEditRow.accept(tag));

    Button delete = new Button(new Icon(VaadinIcon.TRASH));
    delete.setTooltipText(Messages.get("action.delete"));
    delete.addThemeVariants(ButtonVariant.LUMO_TERTIARY_INLINE, ButtonVariant.LUMO_ERROR);
    delete.addClickListener(e -> onDeleteRow.accept(tag));

    actions.add(edit, delete);
    return actions;
  }

  private static String normalize(String s) {
    return s == null ? "" : s.trim().toLowerCase(Locale.ROOT);
  }

  public record TagRow(TagDto tag, long usage) {}
}
