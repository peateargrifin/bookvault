package com.bookvault.bookvault.controller;

import com.bookvault.bookvault.dto.request.UpdateAuthorRequest;
import com.bookvault.bookvault.dto.response.AuthorResponse;
import com.bookvault.bookvault.dto.response.PagedResponse;
import com.bookvault.bookvault.service.AuthorService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/authors")
@RequiredArgsConstructor
@Tag(name = "Authors", description = "Author profile endpoints")
public class AuthorController {

    private final AuthorService authorService;

    // 📘 CONCEPT: Video 6 - Public static route
    @GetMapping
    @Operation(summary = "List all active authors (paginated)")
    public PagedResponse<AuthorResponse> getAllAuthors(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int limit) {
        return authorService.getAllAuthors(page, limit);
    }

    // 📘 CONCEPT: Video 6 - Public dynamic route
    @GetMapping("/{id}")
    @Operation(summary = "Get a single author's public profile")
    public AuthorResponse getAuthor(@PathVariable UUID id) {
        return authorService.getById(id);
    }

    // 📘 CONCEPT: Video 20 - BOLA prevention demonstrated explicitly
    // The path {id} and the authenticated user's id are compared in the service
    @PatchMapping("/{id}")
    @Operation(summary = "Update your own author profile (ownership enforced)")
    public AuthorResponse updateProfile(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateAuthorRequest request,
            @AuthenticationPrincipal String userId) {

        return authorService.updateOwnProfile(
                id, UUID.fromString(userId), request);
    }
}
