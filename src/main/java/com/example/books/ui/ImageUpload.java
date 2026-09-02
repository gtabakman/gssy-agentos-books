package com.example.books.ui;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.component.upload.UploadI18N;
import com.vaadin.flow.component.upload.receivers.MemoryBuffer;
import java.io.IOException;
import java.util.Base64;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

@SuppressWarnings({"deprecation", "removal"})
public class ImageUpload extends HorizontalLayout {

  private final Image thumbnail;
  private final Div placeholder;
  private final Button removeButton;
  private final AtomicReference<byte[]> currentBytes = new AtomicReference<>();
  private final AtomicReference<String> currentMime = new AtomicReference<>();
  private final AtomicBoolean removeExisting = new AtomicBoolean(false);
  private final AtomicBoolean hasOriginalImage = new AtomicBoolean(false);
  private final String alt;
  private final Upload upload;

  public ImageUpload(String alt, int sizePx) {
    this.alt = alt;

    setAlignItems(Alignment.CENTER);
    setWidthFull();
    getStyle().set("flex-wrap", "wrap");

    thumbnail = new Image();
    thumbnail.setAlt(alt);
    thumbnail.setWidth(sizePx + "px");
    thumbnail.setHeight(sizePx + "px");
    thumbnail.getStyle().set("border-radius", "8px");
    thumbnail.getStyle().set("cursor", "pointer");
    thumbnail.getStyle().set("object-fit", "cover");
    thumbnail.setVisible(false);
    thumbnail.addClickListener(e -> openPopup());

    placeholder = new Div();
    placeholder.setWidth(sizePx + "px");
    placeholder.setHeight(sizePx + "px");
    placeholder.getStyle().set("border-radius", "8px");
    placeholder.getStyle().set("box-sizing", "border-box");
    placeholder.getStyle().set("overflow", "hidden");
    placeholder.getStyle().set("background", "var(--lumo-contrast-5pct)");
    placeholder.getStyle().set("border", "1px solid var(--lumo-contrast-20pct)");
    placeholder.getStyle().set("display", "flex");
    placeholder.getStyle().set("align-items", "center");
    placeholder.getStyle().set("justify-content", "center");
    Icon placeholderIcon = new Icon(VaadinIcon.FILE_PICTURE);
    placeholderIcon.setSize(Math.max(16, (int) (sizePx * 0.45)) + "px");
    placeholderIcon.getStyle().set("color", "var(--lumo-secondary-text-color)");
    placeholder.add(placeholderIcon);

    removeButton = new Button(new Icon(VaadinIcon.TRASH));
    removeButton.setTooltipText(Messages.get("imageupload.remove"));
    removeButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY_INLINE, ButtonVariant.LUMO_ERROR);
    removeButton.setEnabled(false);
    removeButton.addClickListener(e -> removeImage());

    MemoryBuffer buffer = new MemoryBuffer();
    upload = new Upload(buffer);
    upload.setAcceptedFileTypes("image/png", "image/jpeg", "image/webp");
    upload.setMaxFiles(1);

    UploadI18N i18n = new UploadI18N();
    i18n.setDropFiles(
        new UploadI18N.DropFiles()
            .setOne(Messages.get("imageupload.dropFile"))
            .setMany(Messages.get("imageupload.dropFiles")));
    i18n.setError(
        new UploadI18N.Error()
            .setIncorrectFileType(Messages.get("imageupload.error.incorrectFileType"))
            .setTooManyFiles(Messages.get("imageupload.error.tooManyFiles"))
            .setFileIsTooBig(Messages.get("imageupload.error.fileIsTooBig")));
    upload.setI18n(i18n);

    Button uploadBtn = new Button(Messages.get("imageupload.choose"), new Icon(VaadinIcon.UPLOAD));
    uploadBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
    upload.setUploadButton(uploadBtn);

    Button cameraButton = new Button(Messages.get("imageupload.camera"), new Icon(VaadinIcon.CAMERA));
    cameraButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
    cameraButton.setWidthFull();
    cameraButton.addClickListener(
        e ->
            upload
                .getElement()
                .executeJs(
                    "const input=this.shadowRoot&&this.shadowRoot.querySelector('input[type=file]');"
                        + "if(input){input.setAttribute('capture','environment');input.click();"
                        + "setTimeout(()=>input.removeAttribute('capture'),0);}"));

    upload.addSucceededListener(
        event -> {
          try {
            byte[] bytes = buffer.getInputStream().readAllBytes();
            setCurrentImage(bytes, event.getMIMEType());
            removeExisting.set(false);
          } catch (IOException ex) {
            throw new RuntimeException(ex);
          }
        });

    Div thumbBox = new Div(placeholder, thumbnail);
    thumbBox.getStyle().set("display", "flex");
    thumbBox.getStyle().set("align-items", "center");
    thumbBox.getStyle().set("justify-content", "center");

    upload.getStyle().set("min-width", "0");
    upload.getStyle().set("max-width", "100%");

    HorizontalLayout imageRow = new HorizontalLayout(upload, thumbBox, removeButton);
    imageRow.setAlignItems(Alignment.CENTER);
    imageRow.setPadding(false);
    imageRow.setWidthFull();
    imageRow.getStyle().set("min-width", "0");
    imageRow.getStyle().set("flex-wrap", "wrap");
    imageRow.setFlexGrow(1, upload);
    imageRow.setFlexGrow(0, thumbBox);
    imageRow.setFlexGrow(0, removeButton);

    VerticalLayout selectionControls = new VerticalLayout(imageRow, cameraButton);
    selectionControls.setPadding(false);
    selectionControls.setSpacing(false);
    selectionControls.setWidthFull();
    selectionControls.getStyle().set("min-width", "0");
    selectionControls.getStyle().set("max-width", "100%");

    add(selectionControls);
    setFlexGrow(1, selectionControls);
  }

  public void setMobileMode(boolean mobile) {
    upload.setDropAllowed(!mobile);
  }

  public void setImage(byte[] bytes, String contentType) {
    hasOriginalImage.set(bytes != null && bytes.length > 0);
    setCurrentImage(bytes, contentType);
    removeExisting.set(false);
  }

  public byte[] getUploadedBytes() {
    return currentBytes.get();
  }

  public String getUploadedContentType() {
    return currentMime.get();
  }

  public boolean isRemoveExistingRequested() {
    return removeExisting.get();
  }

  public boolean isImageSet() {
    byte[] bytes = currentBytes.get();
    return bytes != null && bytes.length > 0;
  }

  private void setCurrentImage(byte[] bytes, String contentType) {
    currentBytes.set(bytes);
    currentMime.set(contentType);
    if (bytes != null && bytes.length > 0) {
      thumbnail.setSrc(toDataUrl(bytes, contentType));
      thumbnail.setVisible(true);
      placeholder.setVisible(false);
      removeButton.setEnabled(true);
    } else {
      thumbnail.setVisible(false);
      placeholder.setVisible(true);
      removeButton.setEnabled(false);
    }
  }

  private void removeImage() {
    currentBytes.set(null);
    currentMime.set(null);
    thumbnail.setVisible(false);
    placeholder.setVisible(true);
    removeButton.setEnabled(false);
    if (hasOriginalImage.get()) {
      removeExisting.set(true);
    }
  }

  private void openPopup() {
    byte[] bytes = currentBytes.get();
    String mime = currentMime.get();
    if (bytes != null && bytes.length > 0) {
      new ImagePopupDialog(toDataUrl(bytes, mime), alt).open();
    }
  }

  private static String toDataUrl(byte[] bytes, String contentType) {
    String ct = contentType != null && !contentType.isBlank() ? contentType : "image/png";
    String b64 = Base64.getEncoder().encodeToString(bytes);
    return "data:" + ct + ";base64," + b64;
  }
}
