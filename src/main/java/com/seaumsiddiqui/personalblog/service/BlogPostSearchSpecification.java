package com.seaumsiddiqui.personalblog.service;


import com.seaumsiddiqui.personalblog.domain.BlogPost;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;

public class BlogPostSearchSpecification{

    public static Specification<BlogPost> buildSearchSpecification(String title, String author, LocalDateTime createdStartDate, LocalDateTime createdEndDate) {
        return Specification.where(title(title))
                .and(author(author))
                .and(createdAt(createdStartDate, createdEndDate));
    }

    private static Specification<BlogPost> title(String title) {
        return (root, query, criteriaBuilder)-> {
            if (title != null && !title.isEmpty()) {
                return criteriaBuilder.like(criteriaBuilder.lower(root.get("title")),"%" + title.toLowerCase() + "%");
            }
            return criteriaBuilder.conjunction();
        };
    }

    private static Specification<BlogPost> author(String author) {
        return (root, query, criteriaBuilder) -> {
            if (author != null && !author.isEmpty()) {
                return criteriaBuilder.like(criteriaBuilder.lower(root.get("author")), "%" + author.toLowerCase() + "%");
            }
            return criteriaBuilder.conjunction();
        };
    }

    private static Specification<BlogPost> createdAt(LocalDateTime createdStartDate, LocalDateTime createdEndDate) {
        return (root, query, criteriaBuilder) -> {
          if (createdStartDate != null && createdEndDate != null) {
              return criteriaBuilder.between(root.get("createdAt"), createdStartDate, createdEndDate);
          } else if (createdStartDate != null) {
              return criteriaBuilder.greaterThanOrEqualTo(root.get("createdAt"), createdStartDate);
          } else if (createdEndDate != null) {
              return criteriaBuilder.lessThanOrEqualTo(root.get("createdAt"), createdEndDate);
          }
          return criteriaBuilder.conjunction();
        };
    }

}
