# MVP Book Library — Shaping Notes

## Scope

Build the complete Phase 1 MVP for the `gssy-agentos-books` family book library. The MVP includes full book CRUD, cover images, wishlist, basic search, tagging, and a responsive mobile/desktop Vaadin Flow UI.

## Decisions

- A single `Book` entity with a `wishlist` boolean handles both owned and wishlist books for the MVP; a future phase can split the workflow when photo/ISBN capture is introduced.
- Tech stack matches the product docs: Vaadin Flow v25.2.6, Java 25, Spring Boot 4.1.1, H2 with disk persistence.
- Pattern and package layout mirror `gssy-contacts-v1`: `domain`, `repo`, `service`, `api/dto`, `ui`, `config`.
- Cover images are stored as a `byte[]` LOB on `Book` and served through a dedicated REST endpoint, following the `profilePicture` pattern in the reference.
- Tags are modeled as a separate entity with a `@ManyToMany` relationship to `Book`.
- Mobile camera/ISBN capture is intentionally out of scope for the MVP (Phase 2).

## Context

- **Visuals:** None provided; UI behavior and constraints were supplied verbally.
- **References:** `gssy-contacts-v1` provides Vaadin Flow, Spring Boot, H2, image upload, tags, responsive card list, dialog, and RTL patterns.
- **Product alignment:** The scope matches the Phase 1 MVP in `agent-os/product/roadmap.md` and the product mission of managing a private domestic book library.

## Verbal UI Specs

- Single RTL direction set in `MainView` with `UI.getCurrent().setDirection(Direction.RIGHT_TO_LEFT)`.
- Use logical directions only (`start`/`end`); avoid physical `right`/`left`.
- All UI literals externalized for i18n.
- Grids render as a card list on mobile.
- Buttons use appropriate icons.
- Input validation messages appear below invalid fields.
- Grids are full width; the last column flexes to fill remaining space.
- No browser scroll; the app fits in the visible area.
- Use the new Aura theme by default; keep a theme-switching hook for the future.
- Build reusable UI components, especially for cover-image upload.

## Post-Generation UI/UX and Feature Changes

The following changes were applied after the initial app generation based on ongoing feedback.

- All UI literals externalized and Hebrew translations added (`messages.properties` / `messages_he.properties`).
- Full RTL direction set in `MainView` and all layouts use logical `start`/`end` values.
- Mobile grids render as responsive card lists; desktop grids are full width and avoid browser scroll.
- Icons added to all action and filter buttons.
- Input validation with inline error messages; forms support “Save and add another” loop mode.
- Author management added as a separate entity, service, repository, REST API, and dedicated page.
- Book form allows creating new authors and tags on the fly via `+` buttons; these fields span two columns.
- Toolbar wishlist checkbox and clear button aligned to the input field middle; grid wishlist column reduced to a checkbox and the tags column widened.
- Book dialog height constrained with `max-height: 90vh` to prevent excessive vertical space.
- Invalid state is explicitly reset after “Save and add another” in author/tag dialogs.
- `ImageUpload` i18n extended to include drop labels and upload error messages.
