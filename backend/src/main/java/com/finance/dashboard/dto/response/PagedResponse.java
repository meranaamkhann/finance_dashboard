package com.finance.dashboard.dto.response;
import lombok.Getter;
import org.springframework.data.domain.Page;
import java.util.List;

@Getter
public class PagedResponse<T> {
    private final List<T> content;
    private final int page;
    private final int size;
    private final long totalElements;
    private final int totalPages;
    private final boolean first;
    private final boolean last;
    public PagedResponse(Page<T> p) { content=p.getContent(); page=p.getNumber(); size=p.getSize(); totalElements=p.getTotalElements(); totalPages=p.getTotalPages(); first=p.isFirst(); last=p.isLast(); }
}
