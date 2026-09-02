package com.example.books.ui;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment;
import com.vaadin.flow.component.orderedlayout.FlexComponent.JustifyContentMode;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.textfield.TextField;
import java.util.function.Consumer;

public class NameEditorDialog extends Dialog {

  private final TextField name = new TextField();

  public NameEditorDialog(
      String title, String initialValue, Consumer<String> onSave, Runnable afterSave, boolean loopMode) {
    setHeaderTitle(title);
    setCloseOnOutsideClick(false);
    getElement().getStyle().set("width", "min(24em, 95vw)");
    getElement().getStyle().set("max-width", "100vw");
    getElement().getStyle().set("max-height", "90vh");

    name.setLabel(Messages.get("tag.name"));
    name.setValue(initialValue != null ? initialValue : "");
    name.setWidthFull();
    name.setRequiredIndicatorVisible(true);
    name.setClearButtonVisible(true);

    add(name);

    Button cancel = new Button(Messages.get("dialog.cancel"), e -> close());
    Button save = new Button(Messages.get("dialog.save"), e -> save(onSave, afterSave, false));
    save.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

    Button saveAndAnother = null;
    if (loopMode) {
      saveAndAnother = new Button(Messages.get("dialog.saveAndAnother"), e -> save(onSave, afterSave, true));
      saveAndAnother.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
    }

    HorizontalLayout startGroup = new HorizontalLayout();
    startGroup.setPadding(false);
    startGroup.setSpacing(true);
    if (saveAndAnother != null) {
      startGroup.add(saveAndAnother);
    }

    HorizontalLayout endGroup = new HorizontalLayout(cancel, save);
    endGroup.setPadding(false);
    endGroup.setSpacing(true);

    HorizontalLayout footer = new HorizontalLayout(startGroup, endGroup);
    footer.setWidthFull();
    footer.setJustifyContentMode(JustifyContentMode.BETWEEN);
    footer.setDefaultVerticalComponentAlignment(Alignment.END);
    getFooter().add(footer);
  }

  private void save(Consumer<String> onSave, Runnable afterSave, boolean andAnother) {
    name.setInvalid(false);
    String value = name.getValue();
    if (value == null || value.isBlank()) {
      name.setInvalid(true);
      name.setErrorMessage(Messages.get("validation.required"));
      return;
    }
    if (value.trim().length() > 80) {
      name.setInvalid(true);
      name.setErrorMessage(Messages.get("validation.tooLong"));
      return;
    }
    try {
      onSave.accept(value.trim());
      if (afterSave != null) {
        afterSave.run();
      }
      if (andAnother) {
        name.clear();
        name.setInvalid(false);
        name.setErrorMessage(null);
        name.focus();
        Notification.show(Messages.get("dialog.savedAndAnother"));
      } else {
        close();
      }
    } catch (Exception ex) {
      Notification.show("Error saving tag");
    }
  }
}
