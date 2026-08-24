package com.example.books.ui;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.Tabs;
import java.util.List;

public class MainTabs extends VerticalLayout {

  public MainTabs(VerticalLayout booksPage, VerticalLayout authorsPage, VerticalLayout tagsPage) {
    setSizeFull();
    setPadding(false);
    setSpacing(false);

    Tabs tabs = new Tabs();
    Tab booksTab = new Tab(Messages.get("books.title"));
    Tab authorsTab = new Tab(Messages.get("authors.title"));
    Tab tagsTab = new Tab(Messages.get("tags.title"));
    tabs.add(booksTab, authorsTab, tagsTab);
    tabs.setWidthFull();

    Div pages = new Div();
    pages.setSizeFull();
    pages.getStyle().set("display", "flex");
    pages.getStyle().set("flex-direction", "column");

    List<VerticalLayout> all = List.of(booksPage, authorsPage, tagsPage);
    pages.add(booksPage, authorsPage, tagsPage);
    showPage(all, booksPage);

    tabs.addSelectedChangeListener(
        e -> {
          if (e.getSelectedTab() == booksTab) {
            showPage(all, booksPage);
          } else if (e.getSelectedTab() == authorsTab) {
            showPage(all, authorsPage);
          } else if (e.getSelectedTab() == tagsTab) {
            showPage(all, tagsPage);
          }
        });

    add(tabs, pages);
    expand(pages);
  }

  private void showPage(List<VerticalLayout> all, VerticalLayout show) {
    for (VerticalLayout page : all) {
      page.setVisible(page == show);
    }
  }
}
