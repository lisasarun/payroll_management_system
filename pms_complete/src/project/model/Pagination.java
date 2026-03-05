package project.model;

import lombok.Getter;

@Getter


public class Pagination {

    private int page;

    private final int size;

    private final int totalItems;

    public Pagination(int page, int size, int totalItems) {
        this.page = Math.max(1, page);
        this.size = size > 0 ? size : 10;
        this.totalItems = totalItems;
    }

    public int getTotalPages() { return (int) Math.ceil((double) totalItems / size); }
    public boolean hasNext()   { return page < getTotalPages(); }
    public boolean hasPrev()   { return page > 1; }
    public void next()         { if (hasNext()) page++; }
    public void prev()         { if (hasPrev()) page--; }
}
