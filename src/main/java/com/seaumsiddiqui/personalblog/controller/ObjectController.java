package com.seaumsiddiqui.personalblog.controller;

import com.seaumsiddiqui.personalblog.service.ObjectStorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RequiredArgsConstructor
@RestController
@RequestMapping("/files")
public class ObjectController {
    private final ObjectStorageService objectStorageService;


    @PostMapping("/upload-markdown")
    public ResponseEntity<String> uploadMarkdownContent(@RequestBody String markdownContent) {
        return ResponseEntity.ok(objectStorageService.uploadMarkdownContent(markdownContent));
    }

    @PostMapping("/upload-image")
    public ResponseEntity<String> uploadImage(@RequestParam MultipartFile image) {
        return ResponseEntity.ok(objectStorageService.uploadImage(image));
    }

    @PutMapping("/update")
    public ResponseEntity<String> updateMarkdownContent(@RequestParam String objectUrl, @RequestBody String markdownContent) {
        objectStorageService.updateMarkdownContent(objectUrl, markdownContent);
        return ResponseEntity.ok("File updated.");
    }

    @DeleteMapping("/delete")
    public ResponseEntity<String> deleteFileObject(@RequestParam String objectUrl) {
        objectStorageService.deleteFileObject(objectUrl);
        return ResponseEntity.ok("File removed!");
    }

}
