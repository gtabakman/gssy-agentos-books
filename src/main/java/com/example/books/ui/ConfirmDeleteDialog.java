package com.example.books.ui;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;

public class ConfirmDeleteDialog extends Dialog {

  public ConfirmDeleteDialog(String message, Runnable onConfirm) {
    setHeaderTitle(Messages.get("dialog.confirm.delete"));
    setCloseOnOutsideClick(false);

    add(new Span(message != null ? message : "Delete?"));

    Button cancel = new Button(new Icon(VaadinIcon.CLOSE_SMALL));
    cancel.setTooltipText(Messages.get("dialog.cancel"));
    cancel.addThemeVariants(ButtonVariant.LUMO_TERTIARY_INLINE);
    cancel.addClickListener(e -> close());

    Button confirm = new Button(new Icon(VaadinIcon.CHECK));
    confirm.setTooltipText(Messages.get("dialog.delete"));
    confirm.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_ERROR);
    confirm.addClickListener(
        e -> {
          if (onConfirm != null) {
            onConfirm.run();
          }
          close();
        });

    getFooter().add(cancel, confirm);
  }
}
