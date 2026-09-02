package com.example.books.ui;

import com.example.books.api.dto.AuthorDto;
import com.example.books.api.dto.CreateAuthorRequest;
import com.example.books.api.dto.UpdateAuthorRequest;
import com.example.books.service.AuthorService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.textfield.TextField;

public class AuthorEditorDialog extends Dialog {

  private final TextField firstName = new TextField(Messages.get("author.firstName"));
  private final TextField lastName = new TextField(Messages.get("author.lastName"));
  private final boolean loopMode;

  public AuthorEditorDialog(
      String title, AuthorDto author, AuthorService authorService, Runnable afterSave) {
    setHeaderTitle(title);
    setCloseOnOutsideClick(false);
    getElement().getStyle().set("width", "min(30em, 95vw)");
    getElement().getStyle().set("max-width", "100vw");
    getElement().getStyle().set("max-height", "90vh");
    this.loopMode = author == null;

    firstName.setValue(author != null ? author.firstName() : "");
    firstName.setWidthFull();
    firstName.setRequiredIndicatorVisible(true);
    firstName.setClearButtonVisible(true);

    lastName.setValue(author != null ? author.lastName() : "");
    lastName.setWidthFull();
    lastName.setClearButtonVisible(true);

    FormLayout form = new FormLayout(firstName, lastName);
    form.setResponsiveSteps(new FormLayout.ResponsiveStep("0", 1), new FormLayout.ResponsiveStep("20em", 2));
    form.setWidthFull();
    add(form);

    Button cancel = new Button(Messages.get("dialog.cancel"), e -> close());
    Button save = new Button(Messages.get("dialog.save"), e -> save(author, authorService, afterSave, false));
    save.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

    Button saveAndAnother = null;
    if (loopMode) {
      saveAndAnother = new Button(Messages.get("dialog.saveAndAnother"), e -> save(author, authorService, afterSave, true));
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
    footer.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
    footer.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.END);
    getFooter().add(footer);
  }

  private void save(AuthorDto author, AuthorService authorService, Runnable afterSave, boolean andAnother) {
    String fn = firstName.getValue();
    firstName.setInvalid(false);
    lastName.setInvalid(false);
    if (fn == null || fn.isBlank()) {
      firstName.setInvalid(true);
      firstName.setErrorMessage(Messages.get("validation.required"));
      return;
    }
    if (fn.trim().length() > 120) {
      firstName.setInvalid(true);
      firstName.setErrorMessage(Messages.get("validation.tooLong"));
      return;
    }
    String ln = lastName.getValue() != null ? lastName.getValue().trim() : "";
    if (ln.length() > 120) {
      lastName.setInvalid(true);
      lastName.setErrorMessage(Messages.get("validation.tooLong"));
      return;
    }
    try {
      if (author == null) {
        CreateAuthorRequest req = new CreateAuthorRequest();
        req.setFirstName(fn.trim());
        req.setLastName(ln.isBlank() ? null : ln);
        authorService.create(req);
      } else {
        UpdateAuthorRequest req = new UpdateAuthorRequest();
        req.setFirstName(fn.trim());
        req.setLastName(ln.isBlank() ? null : ln);
        authorService.update(author.id(), req);
      }
      if (afterSave != null) {
        afterSave.run();
      }
      if (andAnother) {
        firstName.clear();
        firstName.setInvalid(false);
        firstName.setErrorMessage(null);
        lastName.clear();
        lastName.setInvalid(false);
        lastName.setErrorMessage(null);
        firstName.focus();
        Notification.show(Messages.get("dialog.savedAndAnother"));
      } else {
        close();
      }
    } catch (Exception ex) {
      Notification.show("Error saving author");
    }
  }
}
