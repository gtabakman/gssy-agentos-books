---
agent: devin-local
session: stirring-band
created: 2026-08-20T09:32:12Z
---

# Shape Spec: MVP for the Family Book Library

Implement the complete Phase 1 MVP for the `gssy-agentos-books` family book library, covering book CRUD, cover images, wishlist, basic search, tags, and a mobile/desktop responsive Vaadin Flow UI aligned with the product docs and the `gssy-contacts-v1` reference implementation.

## Shaping Notes

### Scope

Build the whole MVP defined in `agent-os/product/roadmap.md`:

- Manage books with full CRUD operations.
- Attach cover images to books.
- Wishlist for books to acquire.
- Basic search across the collection.
- Tagging support.
- Responsive layout optimized for both mobile and desktop.

### Decisions

- Model the library as a single `Book` entity that can be marked as a wishlist item. This satisfies MVP while keeping the schema simple. A future phase can split wishlist into a richer flow when photo/ISBN capture is added.
- Use H2 file persistence, Spring Data JPA, and Vaadin Flow, matching the chosen tech stack.
- Follow the package and pattern structure of `gssy-contacts-v1` (`domain`, `repo`, `service`, `api/dto`, `ui`, `config`).
- Store cover images as a `byte[]` LOB on the `Book` entity, served via a dedicated REST endpoint, just like `profilePicture` in the reference.
- Model tags with a separate `Tag` entity and a `@ManyToMany` relationship to `Book`.
- Postpone mobile-camera/ISBN capture to Phase 2; for the MVP, cover images are uploaded from the device.

### Context

- **Visuals:** None provided; UI behavior and constraints were supplied verbally.
- **References:** `gssy-contacts-v1` (Vaadin Flow + Spring Boot + H2 contacts application). It supplies package layout, service/controller patterns, image upload, many-to-many tagging, mobile card list, dialogs, RTL setup, and responsive grid handling.
- **Product alignment:** The scope directly matches the Phase 1 MVP in `agent-os/product/roadmap.md` and supports the mission of managing a private domestic book library for the family.
- **Standards:** `agent-os/standards/index.yml` is empty, so no additional Agent OS standards apply.

### Verbal UI Specs

- Single RTL direction set in `MainView` with `UI.getCurrent().setDirection(Direction.RIGHT_TO_LEFT)`.
- Use logical directions only (`start`/`end`); avoid physical `right`/`left` values.
- All UI literals must be externalized for i18n.
- Grids must render as a card list on mobile.
- Buttons must use appropriate icons.
- All input validation must show messages below the invalid fields.
- Grids must be full width; the last column must occupy remaining space.
- No browser scroll; the whole app must fit in the visible area.
- Use the new Aura theme by default and keep a hook for switching themes later.
- Build reusable UI components, especially for cover-image upload.

## Implementation Plan

### Task 1: Persist the Shaped Spec

Create `agent-os/specs/2026-08-20-1256-mvp-book-library/` with:

- `plan.md` — this plan
- `shape.md` — shaping notes and decisions
- `standards.md` — standards applied (none besides the ones derived from the reference)
- `references.md` — pointers to `gssy-contacts-v1`
- `visuals/` — empty (no visuals provided)

### Task 2: Create Project Skeleton

- Generate `pom.xml` with `spring-boot-starter-parent` (Spring Boot 4.1.1), `java.version=25`, `vaadin-bom` (v25.2.6), `spring-boot-starter-data-jpa`, `spring-boot-starter-validation`, `h2`, `lombok` (optional), and `spring-boot-maven-plugin`.
- Create package structure `com.example.books` with sub-packages `api`, `api.dto`, `config`, `domain`, `repo`, `service`, `ui`.
- Add `src/main/resources/application.properties` with H2 file datasource, `ddl-auto=update`, `h2.console.enabled=true`, and `vaadin.allowed-packages`.
- Add `src/main/resources/static/favicon.svg` and `src/main/resources/META-INF/resources/styles.css` placeholders.

### Task 3: Configure Application Shell and Theme Hook

- `BooksApplication` as `@SpringBootApplication` with `AppShellConfigurator`, favicon, and default theme annotation.
- Confirm Aura theme availability in Vaadin 25.2.6; if not available, default to Lumo and add a theme-switching extension point.

### Task 4: Implement Domain Model

- `Book` entity with fields: `id`, `title`, `subtitle`, `authors`, `isbn`, `publisher`, `year`, `notes`, `wishlist` (boolean), `coverPicture` (`byte[]` with `@Lob`), `coverPictureContentType` (String), `@ManyToMany` `tags`, and JPA validations (`@NotBlank`, `@Size`).
- `Tag` entity with `id` and `name` (`@NotBlank`, `@Size(max=80)`, `@Column(unique=true)`).
- `NotFoundException` runtime exception.

### Task 5: Implement DTOs and Request Objects

- `BookDto`, `CreateBookRequest`, `UpdateBookRequest` with validation annotations.
- `TagDto`, `CreateTagRequest`, `UpdateTagRequest`.
- `BookListFilter` or equivalent for search parameters.

### Task 6: Implement Repositories

- `BookRepository` extending `JpaRepository` with custom query methods for basic search by title/author/ISBN and tag filter.
- `TagRepository` with a native `deleteTagLinksByTagId` method for safe tag removal.

### Task 7: Implement `TagService`

- List, create, update, delete tags.
- Usage count and safe deletion (clear `book_tags` links before deleting).
- `loadEntities(List<Long>)` helper for many-to-many lookup.

### Task 8: Implement `BookService`

- CRUD for `Book` with tag assignment and validation.
- Basic search: filter by title, author, ISBN, and tags.
- Wishlist listing.
- Cover image set/get/delete.
- `NotFoundException` handling.

### Task 9: Implement REST API

- `BookController` (`/api/books`) with CRUD, cover upload/download/delete endpoints, and search.
- `TagController` (`/api/tags`) with CRUD.
- `ApiExceptionHandler` for `NotFoundException` and validation errors, returning JSON error bodies.

### Task 10: Implement Reusable UI Components

- `ImageUpload` component: upload button, thumbnail preview, remove, popup viewer.
- `ConfirmDeleteDialog` reusable confirmation dialog with icon.
- Logical-start/end layout helpers if needed.

### Task 11: Set Up Externalized Literals and I18n

- `messages.properties` and `messages_he.properties` for Hebrew (default).
- Centralized `I18NProvider` or `Messages` helper used by all UI classes.
- No hard-coded strings in components.

### Task 12: Implement `MainView`, `MainTabs`, and `BooksPage`

- `MainView` sets RTL, applies the Aura/Lumo theme, hosts tabs, listens to viewport width for mobile mode, and owns shared caches.
- `MainTabs` switches between `BooksPage` and `TagsPage`.
- `BooksPage`: toolbar with new/edit/delete, filter bar (title/author/ISBN/tags/wishlist toggle), `Grid`/`Div` card list, cover thumbnails, responsive switching, full-width with last-column flex.

### Task 13: Implement `BookEditorDialog` and `BookEditorForm`

- Dialog for create/edit with `BookEditorForm` (title, subtitle, authors, ISBN, publisher, year, notes, wishlist toggle, tags multi-select).
- `ImageUpload` for cover picture.
- Frontend validation with messages below fields; backend validation on save.
- Create vs. update flow.

### Task 14: Implement `TagsPage`

- CRUD grid for tags with inline or dialog editor.
- Usage count display.
- Delete confirmation with usage warning.

### Task 15: Apply Theme, Styling, and Layout Constraints

- Confirm Aura theme setup or Lumo fallback.
- Ensure `setSizeFull` / `expand` usage so no browser scroll appears.
- Last grid column `setFlexGrow(1)`.
- Button icons on all action buttons.
- CSS logical properties for margins/padding where necessary.

### Task 16: Build and Smoke Test

- Run `mvn -q package` and `mvn spring-boot:run` (or equivalent) to verify the app starts and the MVP flows work.
- Run any generated `ApplicationTests`.

## Files to Create / Modify

- `pom.xml`
- `src/main/resources/application.properties`
- `src/main/resources/META-INF/resources/styles.css`
- `src/main/resources/messages.properties`
- `src/main/resources/messages_he.properties`
- `src/main/java/com/example/books/BooksApplication.java`
- `src/main/java/com/example/books/config/*`
- `src/main/java/com/example/books/domain/Book.java`
- `src/main/java/com/example/books/domain/Tag.java`
- `src/main/java/com/example/books/repo/BookRepository.java`
- `src/main/java/com/example/books/repo/TagRepository.java`
- `src/main/java/com/example/books/service/BookService.java`
- `src/main/java/com/example/books/service/TagService.java`
- `src/main/java/com/example/books/service/NotFoundException.java`
- `src/main/java/com/example/books/api/BookController.java`
- `src/main/java/com/example/books/api/TagController.java`
- `src/main/java/com/example/books/api/ApiExceptionHandler.java`
- `src/main/java/com/example/books/api/dto/*`
- `src/main/java/com/example/books/ui/MainView.java`
- `src/main/java/com/example/books/ui/MainTabs.java`
- `src/main/java/com/example/books/ui/BooksPage.java`
- `src/main/java/com/example/books/ui/BookEditorDialog.java`
- `src/main/java/com/example/books/ui/BookEditorForm.java`
- `src/main/java/com/example/books/ui/TagsPage.java`
- `src/main/java/com/example/books/ui/ImageUpload.java`
- `src/main/java/com/example/books/ui/ConfirmDeleteDialog.java`
- `src/test/...` for smoke tests
- `agent-os/specs/2026-08-20-1256-mvp-book-library/*`

## Verification

- `mvn -q package` succeeds.
- `mvn spring-boot:run` starts without errors.
- All CRUD operations work in the browser.
- Cover image upload, preview, and removal work.
- Tags can be created, assigned, and deleted safely.
- Basic search filters books by title, author, ISBN, and tags.
- Wishlist toggle and filtering work.
- Mobile viewport switches the grid to a card list.
- UI is in RTL and uses externalized literals.

## Risks / Considerations

- **Aura theme availability:** Vaadin 25.2.6 may not include the new Aura theme in this release. If unavailable, default to Lumo and add a theme-switching hook for the future.
- **Camera/ISBN capture:** Out of scope for MVP; this is planned for Phase 2.
- **Cover image storage:** Storing images in H2 as LOB is fine for a single-family, local app. If the library grows, move to filesystem/blob storage.
- **i18n complexity:** Externalizing every literal adds overhead for an MVP. The plan isolates it in a dedicated task and centralizes access to keep it manageable.
- **No browser scroll:** This requires disciplined use of `setSizeFull` and `expand`. It should be verified on both desktop and mobile viewports.

## Post-Generation Implementation Changes

After the initial app generation, the following implementation-level changes were made based on feedback and smoke testing:

- Upgraded `pom.xml` from Spring Boot 4.1.0 to Spring Boot 4.1.1.
- Added SpringDoc OpenAPI/Swagger UI v3.1.0 for API documentation at `/swagger-ui/index.html` and `/v3/api-docs`.
- Added `SeedController` and `SeedService` to provide `POST /api/seed`, which clears existing data and generates 20 Hebrew tags, 20 Hebrew authors, and 150 Hebrew books for development/demo use.
- Added a dedicated `Author` entity, `AuthorRepository`, `AuthorService`, `AuthorController`, and `AuthorsPage` with full CRUD and usage counts.
- Reworked `BookEditorForm` to use `FormItem` labels for authors/tags, place title/subtitle/authors/tags/notes across two columns, and align the `+` buttons to the input field middle.
- Reworked the `BooksPage` toolbar so the search and tag filter use placeholders and the wishlist/clear controls align to the input field middle.
- Tuned the `BooksPage` grid: wishlist column rendered as a compact icon-only checkbox, tags column widened, and row action buttons placed at the end of every row.
- Constrained `BookEditorDialog` height with `max-height: 90vh` and removed the forced `30em` minimum height.
- Added `ImageUpload` i18n via `UploadI18N` and externalized drop labels / error messages.
- Cleared invalid/error state after “Save and add another” in `AuthorEditorDialog` and `NameEditorDialog`.
- Cleaned up IDE/compiler warnings: removed unused fields and imports, replaced `Label` with `Span` for `FormItem` labels, and suppressed deprecation/removal warnings for the Vaadin `Upload` receiver API in `ImageUpload`.
