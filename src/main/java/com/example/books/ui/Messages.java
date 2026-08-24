package com.example.books.ui;

import com.vaadin.flow.component.UI;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.text.MessageFormat;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;

public final class Messages {

  private Messages() {}

  public static String get(String key, Object... args) {
    Locale locale = (UI.getCurrent() != null && UI.getCurrent().getLocale() != null)
        ? UI.getCurrent().getLocale()
        : Locale.forLanguageTag("he-IL");

    String value = lookup(locale, key);
    if (args != null && args.length > 0) {
      return MessageFormat.format(value, args);
    }
    return value;
  }

  private static String lookup(Locale locale, String key) {
    String lang = locale.getLanguage();
    if (lang != null && !lang.isBlank() && !"en".equalsIgnoreCase(lang)) {
      try {
        return load("messages_" + lang).getString(key);
      } catch (MissingResourceException ignored) {
        // fall through to default bundle
      }
    }
    try {
      return load("messages").getString(key);
    } catch (MissingResourceException e) {
      return '!' + key + '!';
    }
  }

  private static ResourceBundle load(String baseName) {
    String path = baseName + ".properties";
    InputStream is = Messages.class.getClassLoader().getResourceAsStream(path);
    if (is == null) {
      throw new MissingResourceException("Missing bundle: " + path, Messages.class.getName(), path);
    }
    try (Reader reader = new InputStreamReader(is, StandardCharsets.UTF_8)) {
      return new PropertyResourceBundle(reader);
    } catch (IOException e) {
      throw new MissingResourceException("Cannot load bundle: " + path, Messages.class.getName(), path);
    }
  }
}
