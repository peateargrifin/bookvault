package com.bookvault.bookvault.controller;

import com.bookvault.bookvault.dto.request.CreateBookRequest;
import com.bookvault.bookvault.dto.request.UpdateBookRequest;
import com.bookvault.bookvault.dto.response.BookResponse;
import com.bookvault.bookvault.dto.response.PagedResponse;
import com.bookvault.bookvault.entity.Book;
import com.bookvault.bookvault.service.BookService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

// 📘 CONCEPT: Video 6 - Routing
// /api/v1/books → static route
// /api/v1/books/{id} → dynamic route with path parameter
// 📘 CONCEPT: Video 11 - Plural resource name (/books not /book)
@RestController
@RequestMapping("/api/v1/books")
@RequiredArgsConstructor
@Tag(name = "Books", description = "Book management endpoints")
@SecurityRequirement(name = "Bearer Authentication")
public class BookController {

    private final BookService bookService;

    // ── Public Endpoints (no auth required) ───────────────────────────────

    // 📘 CONCEPT: Video 11 - List with pagination + filtering + sorting
    // 📘 CONCEPT: Video 9 - Query params with defaults (transformation)
    @GetMapping
    @Operation(summary = "List all published books (paginated)")
    public PagedResponse<BookResponse> getAllBooks(
            // 📘 CONCEPT: Video 11 - Sensible defaults for all query params
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int limit,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortOrder,
            @RequestParam(required = false) Book.BookGenre genre,
            @RequestParam(required = false) Book.BookStatus status,
            @RequestParam(required = false) UUID authorId) {

        return bookService.getAllBooks(
                page, limit, sortBy, sortOrder, genre, status, authorId);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a single published book by ID")
    public BookResponse getBook(@PathVariable UUID id) {
        // 📘 CONCEPT: Video 20 - Returns 404 for both nonexistent AND unpublished
        // Client can't tell the difference → no information leakage
        return bookService.getById(id);
    }

    // ── Authenticated Endpoints ────────────────────────────────────────────

    // 📘 CONCEPT: Video 20 - @AuthenticationPrincipal extracts userId from JWT
    // This is set by JwtAuthFilter in the SecurityContext
    // 🟡 NOVICE: read userId from request body → client can fake any userId
    // 🏢 PRODUCT: userId ALWAYS from verified JWT → cannot be spoofed
    @GetMapping("/my-books")
    @Operation(summary = "Get authenticated author's own books")
    public PagedResponse<BookResponse> getMyBooks(
            @AuthenticationPrincipal String authorId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int limit) {

        return bookService.getAllBooks(
                page, limit, "createdAt", "desc",
                null, null, UUID.fromString(authorId));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create a new book")
    public BookResponse createBook(
            @Valid @RequestBody CreateBookRequest request,
            // 📘 CONCEPT: Video 20 - authorId from JWT, not request body
            @AuthenticationPrincipal String authorId) {

        return bookService.createBook(request, UUID.fromString(authorId));
    }

    // 📘 CONCEPT: Video 11 - PATCH for partial update (not PUT)
    @PatchMapping("/{id}")
    @Operation(summary = "Update a book (partial update)")
    public BookResponse updateBook(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateBookRequest request,
            @AuthenticationPrincipal String authorId) {

        // 📘 CONCEPT: Video 20 - BOLA prevention
        // Service verifies book belongs to this authorId before updating
        return bookService.updateBook(id, request, UUID.fromString(authorId));
    }

    // 📘 CONCEPT: Video 5 - DELETE returns 204 No Content
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Delete a book")
    public void deleteBook(
            @PathVariable UUID id,
            @AuthenticationPrincipal String authorId) {

        bookService.deleteBook(id, UUID.fromString(authorId));
    }

    // 📘 CONCEPT: Video 11 - Custom action (not CRUD)
    // POST /books/{id}/publish → triggers business logic beyond simple field update
    // 🟡 NOVICE: PATCH /books/{id} with {"status": "PUBLISHED"}
    //             → server can't run business validation before status change
    // 🏢 PRODUCT: custom action endpoint → validates price, description, etc.
    //             before allowing the state transition
    @PostMapping("/{id}/publish")
    @Operation(summary = "Publish a book (custom action)")
    public BookResponse publishBook(
            @PathVariable UUID id,
            @AuthenticationPrincipal String authorId) {

        return bookService.publishBook(id, UUID.fromString(authorId));
    }

    // 📘 CONCEPT: Video 20 - Admin-only endpoint (BFLA prevention)
    // @PreAuthorize checks role BEFORE method body runs
    // Non-admin gets 403 → GlobalExceptionHandler converts to 404
    @GetMapping("/admin/all")
    @org.springframework.security.access.prepost.PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "ADMIN: Get all books regardless of status or owner")
    public PagedResponse<BookResponse> getAllBooksAdmin(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int limit) {

        return bookService.getAllBooks(
                page, limit, "createdAt", "desc", null, null, null);
    }
}
