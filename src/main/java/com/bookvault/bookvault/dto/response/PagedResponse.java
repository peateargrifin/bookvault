package com.bookvault.bookvault.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

// 📘 CONCEPT: Video 11 - Consistent paginated response structure
// 🟡 NOVICE: return raw List<Book> → client doesn't know total count, can't paginate
// 🏢 PRODUCT: every list endpoint returns this envelope
//             client knows: how many total, what page, how many pages
//             Swiggy/Zomato use this for restaurant listing, infinite scroll
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PagedResponse<T> {

    private List<T> data;
    private int page;
    private int limit;
    private long total;
    private int totalPages;
    private boolean hasNext;
    private boolean hasPrevious;

    public static <T> PagedResponse<T> of(
            List<T> data, int page, int limit, long total) {
        int totalPages = (int) Math.ceil((double) total / limit);
        return PagedResponse.<T>builder()
                .data(data)
                .page(page)
                .limit(limit)
                .total(total)
                .totalPages(totalPages)
                .hasNext(page < totalPages)
                .hasPrevious(page > 1)
                .build();
    }
}
