# Standards for MVP Book Library

The following standards apply to this work.

## Agent OS Standards

- `agent-os/standards/index.yml` is currently empty, so no additional Agent OS standards are required.

## Derived Standards from the Reference

- **Package layout:** `domain`, `repo`, `service`, `api/dto`, `ui`, `config` under a single application package.
- **REST conventions:** Standard Spring MVC controller pattern with `GET`, `POST`, `PUT`, `DELETE`, and validation exception handling through `ApiExceptionHandler`.
- **DTO pattern:** Use Java records for responses and validation-annotated request classes for create/update operations.
- **Image handling:** Cover images are uploaded via multipart form data, stored as a `byte[]` LOB, and downloaded through a dedicated `/{id}/cover` endpoint.
- **Reusable UI components:** Dialogs, upload helpers, and confirmation components are extracted to separate classes.

## Post-Generation Standards and Conventions

- **i18n:** All user-facing strings live in `messages.properties` and `messages_he.properties` and are accessed through `Messages.get(...)`. No hard-coded literals in UI code.
- **RTL and logical directions:** `MainView` sets `Direction.RIGHT_TO_LEFT`; layouts use `start`/`end`, `setDefaultVerticalComponentAlignment`, and `JustifyContentMode` rather than physical `left`/`right`.
- **Validation:** Client- and server-side validation use `setInvalid(true)` with localized `setErrorMessage(...)`; forms call `clearAllInvalid()` before each save attempt.
- **Loop mode for new entities:** New book/author/tag dialogs support a “Save and add another” flow that clears the form and resets invalid state.
- **Lint hygiene:** Unused fields and imports are removed; deprecated API usage is either migrated or suppressed with `@SuppressWarnings` and documented when the new API is not appropriate for the current scope.
- **Responsive / mobile-first:** All pages, toolbars, and dialogs must adapt to narrow viewports. Layouts use wrapping flex containers, dialogs use `min()`/`max-width` sizing, and mobile card lists expose the same actions as desktop grid rows.
- **Theme:** Vaadin 25.2.6 uses the Lumo theme as the default; Aura is not available, so a future theme-switching extension point is left in place.
