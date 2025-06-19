package com.seaumsiddiqui.personalblog.service;

import com.seaumsiddiqui.personalblog.domain.BlogPost;
import com.seaumsiddiqui.personalblog.exception.ResourceNotFoundException;
import com.seaumsiddiqui.personalblog.repository.BlogPostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@RequiredArgsConstructor
@Transactional
@Service
public class BlogPostService {
    private final BlogPostRepository blogPostRepository;
    private final ObjectStorageService objectStorageService;


    /** Blog Post Service Methods **/
    public Page<BlogPost> getAllBlogPost(String title, String author, LocalDateTime createdStartDate, LocalDateTime createdEndDate, String sortField, String sortDirection, int page, int size) {
        Specification<BlogPost> searchSpecification = BlogPostSearchSpecification.buildSearchSpecification(title, author, createdStartDate, createdEndDate);

        Sort.Direction direction = Sort.Direction.fromString(sortDirection);
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortField));

        return blogPostRepository.findAll(searchSpecification, pageable);
    }

    public BlogPost getBlogPostById(String id) {
        return blogPostRepository.findById(id)
                .orElseThrow(()-> new ResourceNotFoundException("Blog Post: {%s} not found".formatted(id)));
    }

    public BlogPost createBlogPost(BlogPost blogPost) {
        return blogPostRepository.save(blogPost);
    }

    public BlogPost updateBlogPost(String id, BlogPost updatedPost) {
        BlogPost existingPost = blogPostRepository.findById(id)
                .orElseThrow(()-> new ResourceNotFoundException("Blog Post: {%s} not found".formatted(id)));

        existingPost.setTitle(updatedPost.getTitle());
        existingPost.setAuthor((updatedPost.getAuthor()));
        existingPost.setDescription(updatedPost.getDescription());
        existingPost.setTags(updatedPost.getTags());
        existingPost.setRelatedPostURLs(updatedPost.getRelatedPostURLs());
        existingPost.setImageURLs(updatedPost.getRelatedPostURLs());
        if (updatedPost.getMarkdownFileURL() != null && !updatedPost.getMarkdownFileURL().isEmpty()) {
            existingPost.setMarkdownFileURL(updatedPost.getMarkdownFileURL());
        }

        return blogPostRepository.save(existingPost);
    }

    public void deleteBlogPost(String id) {
        BlogPost blogPost = getBlogPostById(id);

        try {
            // delete images related to the blogpost form OCI bucket
            if (blogPost.getImageURLs() != null) {
                for (String imageURL: blogPost.getImageURLs()) {
                    objectStorageService.deleteFileObject(imageURL);
                }
            }

            // delete markdown file
            if (blogPost.getMarkdownFileURL() != null) {
                objectStorageService.deleteFileObject(blogPost.getMarkdownFileURL());
            }

            blogPostRepository.deleteById(id);
        } catch(Exception e) {
            throw new ResourceNotFoundException("Blog Post: {%s} not found for deletion".formatted(id));
        }
    }

}
