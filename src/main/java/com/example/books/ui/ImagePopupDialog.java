package com.example.books.ui;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Image;

public class ImagePopupDialog extends Dialog {

  public ImagePopupDialog(String dataUrl, String alt) {
    setCloseOnOutsideClick(true);
    setCloseOnEsc(true);
    setWidth("90vw");
    setHeight("90vh");

    Button close = new Button(Messages.get("imagepopup.close"), e -> close());
    close.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

    Image img = new Image();
    img.setAlt(alt);
    img.setWidthFull();
    img.setHeightFull();
    img.getStyle().set("object-fit", "contain");
    img.setSrc(dataUrl);

    Div box = new Div(img);
    box.setSizeFull();
    box.getStyle().set("display", "flex");
    box.getStyle().set("align-items", "center");
    box.getStyle().set("justify-content", "center");

    add(box);
    getHeader().add(close);
  }
}
