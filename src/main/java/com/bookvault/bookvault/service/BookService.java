package com.bookvault.bookvault.service;

import com.bookvault.bookvault.dto.request.CreateBookRequest;
import com.bookvault.bookvault.dto.request.UpdateBookRequest;
import com.bookvault.bookvault.dto.response.BookResponse;
import com.bookvault.bookvault.dto.response.PagedResponse;
import com.bookvault.bookvault.entity.Author;
import com.bookvault.bookvault.entity.Book;
import com.bookvault.bookvault.entity.Tag;
import com.bookvault.bookvault.exception.BusinessException;
import com.bookvault.bookvault.exception.ResourceNotFoundException;
import com.bookvault.bookvault.repository.AuthorRepository;
import com.bookvault.bookvault.repository.BookRepository;
import com.bookvault.bookvault.repository.TagRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

// 📘 CONCEPT: Video 10 - Service Layer responsibilities:
// 1. Orchestrate repository calls
// 2. Apply business rules
// 3. Never deal with HTTP concerns (no HttpServletRequest here)
@Service
@RequiredArgsConstructor
@Slf4j
public class BookService {

    private final BookRepository bookRepository;
    private final AuthorRepository authorRepository;
    private final TagRepository tagRepository;
    private final CacheService cacheService;
    private final EmailJobService emailJobService;

    // 📘 CONCEPT: Video 11 - List with pagination + filtering + sorting
    @Transactional(readOnly = true)
    public PagedResponse<BookResponse> getAllBooks(
            int page, int limit,
            String sortBy, String sortOrder,
            Book.BookGenre genre,
            Book.BookStatus status,
            UUID authorId) {

        int safePage  = Math.max(1, page);
        int safeLimit = Math.min(Math.max(1, limit), 100);

        // 📘 CONCEPT: Video 13 - Cache key includes all filter params
        // Different filters = different cache entries
        // 🟡 NOVICE: one cache key for all books → filters ignored from cache
        // 🏢 PRODUCT: cache key encodes every query param
        String cacheKey = String.format("books:list:p%d:l%d:s%s:o%s:g%s:st%s:a%s",
                safePage, safeLimit, sortBy, sortOrder,
                genre, status, authorId);

        // Check cache first
        @SuppressWarnings("unchecked")
        PagedResponse<BookResponse> cached =
                (PagedResponse<BookResponse>) cacheService.get(cacheKey);
        if (cached != null) {
            return cached;
        }

        // Cache miss — hit DB
        Sort sort = sortOrder.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();

        Pageable pageable = PageRequest.of(safePage - 1, safeLimit, sort);
        Page<Book> booksPage = bookRepository.findWithFilters(
                genre, status, authorId, pageable);

        PagedResponse<BookResponse> result = PagedResponse.of(
                booksPage.getContent().stream()
                        .map(BookResponse::from)
                        .toList(),
                safePage, safeLimit,
                booksPage.getTotalElements()
        );

        // Store in cache
        // 📘 CONCEPT: Video 13 - Write-through on read (cache-aside)
        cacheService.set(cacheKey, result, Duration.ofMinutes(5));

        return result;
    }

    // 📘 CONCEPT: Video 20 - BOLA Prevention
    // getById does NOT check ownership — used for public book viewing
    // Only PUBLISHED books are accessible without ownership check
    @Transactional(readOnly = true)
    public BookResponse getById(UUID id) {
        String cacheKey = "book:" + id;

        BookResponse cached = (BookResponse) cacheService.get(cacheKey);
        if (cached != null) return cached;

        Book book = bookRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.book(id));

        if (book.getStatus() != Book.BookStatus.PUBLISHED) {
            throw ResourceNotFoundException.book(id);
        }

        BookResponse response = BookResponse.from(book);
        cacheService.set(cacheKey, response, Duration.ofMinutes(10));
        return response;
    }

    // 📘 CONCEPT: Video 20 - BOLA Prevention for owned resources
    // Author can only see their OWN books (draft/published/archived)
    @Transactional(readOnly = true)
    public BookResponse getByIdForOwner(UUID id, UUID authorId) {
        // 📘 CONCEPT: Video 20 - Single query with ownership check
        // Never: find by ID first, then check ownership
        // Always: WHERE id = ? AND author_id = ? in one query
        // If no rows → 404 (attacker can't tell if exists but unauthorized)
        return bookRepository.findByIdAndAuthorId(id, authorId)
                .map(BookResponse::from)
                .orElseThrow(() -> ResourceNotFoundException.book(id));
    }

    @Transactional
    public BookResponse createBook(CreateBookRequest request, UUID authorId) {

        Author author = authorRepository.findById(authorId)
                .orElseThrow(() -> ResourceNotFoundException.author(authorId));

        // 📘 CONCEPT: Video 20 - Business rule: unique ISBN
        if (request.getIsbn() != null
                && bookRepository.existsByIsbn(request.getIsbn())) {
            throw new BusinessException(
                    "A book with this ISBN already exists");
        }

        Set<Tag> tags = new HashSet<>();
        if (request.getTagIds() != null && !request.getTagIds().isEmpty()) {
            tags = new HashSet<>(tagRepository.findAllById(request.getTagIds()));
        }

        Book book = Book.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .isbn(request.getIsbn())
                .price(request.getPrice())
                .genre(request.getGenre() != null
                        ? request.getGenre() : Book.BookGenre.OTHER)
                .publishedDate(request.getPublishedDate())
                .coverImageUrl(request.getCoverImageUrl())
                .author(author)
                .tags(tags)
                .status(Book.BookStatus.DRAFT)
                .build();

        Book saved = bookRepository.save(book);

        // 📘 CONCEPT: Video 13 - Cache invalidation on write
        // New book created → all listing caches are stale → purge them
        cacheService.deleteByPattern("books:list:*");

        log.info("BOOK_CREATED bookId={} authorId={} title={}",
                saved.getId(), authorId, saved.getTitle());

        return BookResponse.from(saved);
    }

    @Transactional
    public BookResponse updateBook(UUID id, UpdateBookRequest request,
            UUID authorId) {

        // 📘 CONCEPT: Video 20 - Ownership verified in query
        Book book = bookRepository.findByIdAndAuthorId(id, authorId)
                .orElseThrow(() -> ResourceNotFoundException.book(id));

        // 📘 CONCEPT: Video 11 - PATCH semantics: only update provided fields
        if (request.getTitle()        != null) book.setTitle(request.getTitle());
        if (request.getDescription()  != null) book.setDescription(request.getDescription());
        if (request.getPrice()        != null) book.setPrice(request.getPrice());
        if (request.getGenre()        != null) book.setGenre(request.getGenre());
        if (request.getPublishedDate()!= null) book.setPublishedDate(request.getPublishedDate());
        if (request.getCoverImageUrl()!= null) book.setCoverImageUrl(request.getCoverImageUrl());

        if (request.getTagIds() != null) {
            Set<Tag> tags = new HashSet<>(
                    tagRepository.findAllById(request.getTagIds()));
            book.setTags(tags);
        }

        Book saved = bookRepository.save(book);
        
        cacheService.deleteByPattern("books:list:*");
        cacheService.delete("book:" + id);

        log.info("BOOK_UPDATED bookId={} authorId={}", id, authorId);
        return BookResponse.from(saved);
    }

    @Transactional
    public void deleteBook(UUID id, UUID authorId) {
        // 📘 CONCEPT: Video 20 - Ownership verified before delete
        Book book = bookRepository.findByIdAndAuthorId(id, authorId)
                .orElseThrow(() -> ResourceNotFoundException.book(id));

        bookRepository.delete(book);
        
        cacheService.deleteByPattern("books:list:*");
        cacheService.delete("book:" + id);

        log.info("BOOK_DELETED bookId={} authorId={}", id, authorId);
    }

    // 📘 CONCEPT: Video 11 - Custom action (not a CRUD operation)
    // POST /books/{id}/publish → changes status + business validation
    // 🟡 NOVICE: use PATCH /books/{id} with status=PUBLISHED
    // 🏢 PRODUCT: custom action → server validates all publish requirements
    //             (price set, description exists, etc.) before allowing publish
    @Transactional
    public BookResponse publishBook(UUID id, UUID authorId) {
        Book book = bookRepository.findByIdAndAuthorId(id, authorId)
                .orElseThrow(() -> ResourceNotFoundException.book(id));

        if (book.getStatus() == Book.BookStatus.ARCHIVED) {
            throw new BusinessException(
                    "Archived books cannot be published directly. " +
                    "Please create a new book.");
        }

        if (book.getPrice() == null) {
            throw new BusinessException(
                    "Book must have a price before publishing");
        }

        book.setStatus(Book.BookStatus.PUBLISHED);
        Book saved = bookRepository.save(book);

        cacheService.deleteByPattern("books:list:*");
        cacheService.delete("book:" + id);

        emailJobService.sendBookPublishedNotification(
                saved.getId(),
                UUID.fromString(authorId.toString()),
                saved.getTitle());

        log.info("BOOK_PUBLISHED bookId={} authorId={}", id, authorId);
        return BookResponse.from(saved);
    }
}
