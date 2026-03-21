package project.dto;

public record PaginationRequest(int page, int size, String sortBy, String sortDirection, String searchKeyword) {
    public PaginationRequest {
        if (page < 1) page = 1;
        if (size < 1) size = 10;
        if (size > 100) size = 100;
        if (sortBy == null) sortBy = "id";
        if (sortDirection == null) sortDirection = "DESC";
        if (searchKeyword == null) searchKeyword = "";
    }


    public PaginationRequest(int page, int size) {
        this(page, size, "id", "DESC", "");
    }

    public PaginationRequest(int page, int size, String sortBy, String sortDirection) {
        this(page, size, sortBy, sortDirection, "");
    }

    public PaginationRequest(int page, int size, String searchKeyword) {
        this(page, size, "id", "DESC", searchKeyword);
    }

    public int getOffset() { return (page - 1) * size; }
    public boolean hasSearch() { return searchKeyword != null && !searchKeyword.trim().isEmpty(); }
    public String getSearchTerm() { return hasSearch() ? searchKeyword.trim() : null; }

    public PaginationRequest withPage(int newPage) {
        return new PaginationRequest(newPage, size, sortBy, sortDirection, searchKeyword);
    }

    public PaginationRequest withSize(int newSize) {
        return new PaginationRequest(page, newSize, sortBy, sortDirection, searchKeyword);
    }
}