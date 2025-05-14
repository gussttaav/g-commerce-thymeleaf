package com.gplanet.commerce.dtos.pagination;

import java.util.List;

import org.springframework.data.domain.Page;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Generic paginated response DTO that wraps any type of content with pagination information.
 *
 * @param <T> The type of content being paginated
 * 
 * @author Gustavo
 * @version 1.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaginatedResponse<T> {
  private List<T> content;
  private int pageNumber;
  private int pageSize;
  private long totalElements;
  private int totalPages;
  private boolean isLastPage;

  /**
   * Creates a PaginatedResponse from a Spring Page object.
   *
   * @param <T> The type of content being paginated
   * @param page The Spring Page object to convert
   * @return A new PaginatedResponse containing the page information
   */
  public static <T> PaginatedResponse<T> fromPage(Page<T> page) {
      return new PaginatedResponse<>(
          page.getContent(),
          page.getNumber(),
          page.getSize(),
          page.getTotalElements(),
          page.getTotalPages(),
          page.isLast()
      );
  }
}
