package com.bookvault.bookvault.service;

import com.bookvault.bookvault.dto.request.UpdateAuthorRequest;
import com.bookvault.bookvault.dto.response.AuthorResponse;
import com.bookvault.bookvault.dto.response.PagedResponse;
import com.bookvault.bookvault.entity.Author;
import com.bookvault.bookvault.exception.ResourceNotFoundException;
import com.bookvault.bookvault.repository.AuthorRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthorService {

    private final AuthorRepository authorRepository;
    private final CacheService cacheService;

    // 📘 CONCEPT: Video 11 - Public listing, no ownership filtering needed
    // Anyone can browse the author directory
    @Transactional(readOnly = true)
    public PagedResponse<AuthorResponse> getAllAuthors(int page, int limit) {

        int safePage  = Math.max(1, page);
        int safeLimit = Math.min(Math.max(1, limit), 100);

        String cacheKey = "authors:list:p" + safePage + ":l" + safeLimit;

        @SuppressWarnings("unchecked")
        PagedResponse<AuthorResponse> cached =
                (PagedResponse<AuthorResponse>) cacheService.get(cacheKey);
        if (cached != null) return cached;

        Pageable pageable = PageRequest.of(
                safePage - 1, safeLimit,
                Sort.by("createdAt").descending());

        Page<Author> authorsPage = authorRepository.findByStatus(
                Author.AuthorStatus.ACTIVE, pageable);

        PagedResponse<AuthorResponse> result = PagedResponse.of(
                authorsPage.getContent().stream()
                        .map(AuthorResponse::from)
                        .toList(),
                safePage, safeLimit,
                authorsPage.getTotalElements()
        );

        cacheService.set(cacheKey, result, Duration.ofMinutes(15));
        return result;
    }

    // 📘 CONCEPT: Video 11 - Public author profile (anyone can view)
    @Transactional(readOnly = true)
    public AuthorResponse getById(UUID id) {
        String cacheKey = "author:" + id;

        AuthorResponse cached = (AuthorResponse) cacheService.get(cacheKey);
        if (cached != null) return cached;

        Author author = authorRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.author(id));

        AuthorResponse response = AuthorResponse.from(author);
        cacheService.set(cacheKey, response, Duration.ofMinutes(15));
        return response;
    }

    // 📘 CONCEPT: Video 20 - BOLA Prevention
    // Author can ONLY update their own profile
    // 🟡 NOVICE: PATCH /authors/{id} → any logged in user can edit ANY profile
    // 🏢 PRODUCT: id from path MUST match authenticated user's id
    @Transactional
    public AuthorResponse updateOwnProfile(UUID id, UUID requestingUserId,
            UpdateAuthorRequest request) {

        // 📘 CONCEPT: Video 20 - Critical ownership check
        // This is THE check that prevents horizontal privilege escalation
        if (!id.equals(requestingUserId)) {
            // 📘 CONCEPT: Video 20 - 403 → 404 masking
            // Don't reveal that author with this ID exists
            // GlobalExceptionHandler converts AccessDeniedException → 404
            log.warn("AUTHORIZATION_DENIED attemptedId={} actualUserId={}",
                    id, requestingUserId);
            throw new org.springframework.security.access.AccessDeniedException(
                    "Cannot edit another author's profile");
        }

        Author author = authorRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.author(id));

        if (request.getName() != null) author.setName(request.getName());
        if (request.getBio()  != null) author.setBio(request.getBio());

        Author saved = authorRepository.save(author);

        // Invalidate caches
        cacheService.delete("author:" + id);
        cacheService.deleteByPattern("authors:list:*");

        log.info("AUTHOR_UPDATED authorId={}", id);
        return AuthorResponse.from(saved);
    }
}
