# References for MVP Book Library

## Similar Implementations

### gssy-contacts-v1

- **Location:** `C:\dev\projects\windsurf\gssy-contacts-v1`
- **Relevance:** A complete Vaadin Flow + Spring Boot + H2 contacts application that demonstrates the desired architecture, UI patterns, image upload, tagging, responsive card lists, RTL, and dialog patterns.
- **Key patterns to borrow:**
  - Maven POM and Spring Boot application configuration.
  - Entity/repository/service/controller layering.
  - `byte[]` LOB image with REST upload/download.
  - Many-to-many tagging.
  - `MainView` with RTL, viewport-based mobile card list, and tabs.
  - `ContactsPage` with filter bar, grid, and mobile cards.
  - `ContactEditorDialog`/`ContactEditorForm` with upload, validation, and lookups.
  - `ConfirmDeleteSimpleDialog` and `ImagePopupDialog`.
  - `ApiExceptionHandler` for validation and not-found errors.
