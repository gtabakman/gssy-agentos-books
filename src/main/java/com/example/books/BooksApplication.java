package com.example.books;

import com.vaadin.flow.component.dependency.StyleSheet;
import com.vaadin.flow.component.page.AppShellConfigurator;
import com.vaadin.flow.server.AppShellSettings;
import com.example.books.ui.Messages;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@StyleSheet("styles.css")
public class BooksApplication implements AppShellConfigurator {

  @Override
  public void configurePage(AppShellSettings settings) {
    settings.addFavIcon("icon", "favicon.svg", "image/svg+xml");
    settings.setPageTitle(Messages.get("app.title"));
  }

  public static void main(String[] args) {
    SpringApplication.run(BooksApplication.class, args);
  }
}
