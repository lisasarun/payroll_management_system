package project.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.Collections;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaginatedResponse<T> {
    private List<T> data;
    private int currentPage;
    private int pageSize;
    private long totalElements;
    private String sortBy;
    private String sortDirection;
    private String searchKeyword;
    private Long executionTimeMs;

    public static <T> PaginatedResponse<T> empty(int page, int size) {
        return new PaginatedResponse<>(Collections.emptyList(), page, size, 0, null, null, null, null);
    }

    public static <T> PaginatedResponse<T> of(List<T> data, int page, int size, long totalElements) {
        return new PaginatedResponse<>(data, page, size, totalElements, null, null, null, null);
    }

    public long getTotalPages() {
        if (pageSize == 0) return 0;
        return (totalElements + pageSize - 1) / pageSize;
    }

    public boolean hasNext() { return currentPage < getTotalPages(); }
    public boolean hasPrevious() { return currentPage > 1; }
    public boolean isEmpty() { return data == null || data.isEmpty(); }
    public int getNextPage() { return hasNext() ? currentPage + 1 : currentPage; }
    public int getPreviousPage() { return hasPrevious() ? currentPage - 1 : currentPage; }

    public String getPaginationSummary() {
        if (isEmpty() || totalElements == 0) return "No records found";
        int start = ((currentPage - 1) * pageSize) + 1;
        int end = (int) Math.min((long) currentPage * pageSize, totalElements);
        return String.format("Showing %d - %d of %d records", start, end, totalElements);
    }

    public String getPageInfo() {
        return String.format("Page %d of %d", currentPage, getTotalPages());
    }
}