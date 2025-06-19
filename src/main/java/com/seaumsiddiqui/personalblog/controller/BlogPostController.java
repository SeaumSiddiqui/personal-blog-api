package com.seaumsiddiqui.personalblog.controller;

import com.seaumsiddiqui.personalblog.domain.BlogPost;
import com.seaumsiddiqui.personalblog.service.BlogPostService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RequiredArgsConstructor
@RestController
@RequestMapping("/blogs")
public class BlogPostController {
    private final BlogPostService blogPostService;


    @GetMapping
    public ResponseEntity<Page<BlogPost>> getAllBlogPost(@RequestParam(required = false) String title,
                                                         @RequestParam(required = false) String author,
                                                         @RequestParam(required = false) LocalDateTime createdStartDate,
                                                         @RequestParam(required = false) LocalDateTime createdEndDate,
                                                         @RequestParam(defaultValue = "createdAt") String sortField,
                                                         @RequestParam(defaultValue = "DESC") String sortDirection,
                                                         @RequestParam(defaultValue = "0") int page,
                                                         @RequestParam(defaultValue = "6") int size) {

        return ResponseEntity.ok(blogPostService
                .getAllBlogPost(title, author, createdStartDate, createdEndDate, sortField, sortDirection, page, size));
    }

    @GetMapping("/{id}")
    public ResponseEntity<BlogPost> getBlogPostById(@PathVariable String id) {
        return ResponseEntity.ok(blogPostService.getBlogPostById(id));
    }

    @PostMapping("/protected")
    public ResponseEntity<BlogPost> createBlogPost(@RequestBody BlogPost blogPost) {
        return ResponseEntity.ok(blogPostService.createBlogPost(blogPost));
    }

    @PutMapping("/protected/{id}")
    public ResponseEntity<BlogPost> updateBlogPost(@PathVariable String id, @RequestPart("blogpost") BlogPost blogPost) {
        return ResponseEntity.ok(blogPostService.updateBlogPost(id, blogPost));
    }

    @DeleteMapping("/protected/{id}")
    public ResponseEntity<String> deleteBlogPost(@PathVariable String id) {
        blogPostService.deleteBlogPost(id);
        return ResponseEntity.ok("Post removed!");
    }

}

