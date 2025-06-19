package com.seaumsiddiqui.personalblog.service;

import com.oracle.bmc.model.BmcException;
import com.oracle.bmc.objectstorage.requests.DeleteObjectRequest;
import com.oracle.bmc.objectstorage.requests.PutObjectRequest;
import com.seaumsiddiqui.personalblog.storage.OracleClientConfiguration;
import com.seaumsiddiqui.personalblog.storage.CloudConfigurationProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
@Service
public class ObjectStorageService {
    private final CloudConfigurationProperties cloudConfiguration;
    private final OracleClientConfiguration clientConfiguration;

    private static final Charset UTF_8 = StandardCharsets.UTF_8;

    // MARKDOWN UPLOAD
    public String uploadMarkdownContent(String content) {
        String objectName = generateMarkdownFileName();
        return uploadStringContent(content, objectName);
    }

    public void updateMarkdownContent(String objectUrl, String content) {
        String objectName = extractEncodedObjectName(objectUrl);
        uploadStringContent(content, objectName);
    }

    private String uploadStringContent(String content, String objectName) {
        try (InputStream inputStream = new ByteArrayInputStream(content.getBytes(UTF_8))) {
            PutObjectRequest request = PutObjectRequest.builder()
                    .namespaceName(cloudConfiguration.getNamespace())
                    .bucketName(cloudConfiguration.getBucketName())
                    .objectName(objectName)
                    .putObjectBody(inputStream)
                    .contentType("text/markdown")
                    .contentLength((long) content.getBytes(UTF_8).length)
                    .build();

            clientConfiguration.getObjectStorage().putObject(request);
            return createObjectUrl(objectName);

        } catch (IOException e) {
            throw new RuntimeException("Error uploading markdown file", e);
        }
    }

    // IMAGE UPLOAD
    public String uploadImage(MultipartFile file) {
        String objectName = generateImageFileName(file.getOriginalFilename());

        try (InputStream inputStream = file.getInputStream()) {
            PutObjectRequest request = PutObjectRequest.builder()
                    .namespaceName(cloudConfiguration.getNamespace())
                    .bucketName(cloudConfiguration.getBucketName())
                    .objectName(objectName)
                    .putObjectBody(inputStream)
                    .contentType(file.getContentType())
                    .contentLength(file.getSize())
                    .build();

            clientConfiguration.getObjectStorage().putObject(request);
            return createObjectUrl(objectName);

        } catch (IOException e) {
            throw new RuntimeException("Error uploading image", e);
        }
    }

    // FILE DELETION
    public void deleteFileObject(String objectUrl) {
        String objectName = extractEncodedObjectName(objectUrl);

        DeleteObjectRequest request = DeleteObjectRequest.builder()
                .namespaceName(cloudConfiguration.getNamespace())
                .bucketName(cloudConfiguration.getBucketName())
                .objectName(objectName)
                .build();

        try {
            clientConfiguration.getObjectStorage().deleteObject(request);
            log.info("Deleted object: {}", objectName);
        } catch (BmcException e) {
            log.error("Failed to delete object from OCI bucket. Status: {}, Code: {}, Message: {}, OPC-Request-ID: {}",
                    e.getStatusCode(), e.getServiceCode(), e.getMessage(), e.getOpcRequestId(), e);
            throw new RuntimeException("Object deletion failed: " + e.getMessage(), e);
        } catch (Exception e) {
            log.error("Unexpected error during object deletion: {}", e.getMessage(), e);
            throw new RuntimeException("Unexpected error during deletion: " + e.getMessage(), e);
        }
    }


    // HELPERS
    private String createObjectUrl(String objectName) {
        return String.format("https://objectstorage.%s.oraclecloud.com/n/%s/b/%s/o/%s",
                cloudConfiguration.getRegion(),
                cloudConfiguration.getNamespace(),
                cloudConfiguration.getBucketName(),
                encodeObjectName(objectName));
    }

    private String encodeObjectName(String s) {
        return URLEncoder.encode(s, UTF_8)
                .replace("+", "%20"); // ensure spaces remain encoded as %20
    }

    private String extractEncodedObjectName(String objectUrl) {
        String[] section = objectUrl.split("/o/", 2);
        if (section.length == 2) {
            return section[1]; // return the raw (still encoded) name
        }
        throw new IllegalArgumentException("Invalid URL format, unable to extract object name.");
    }

    private String generateMarkdownFileName() {
        return UUID.randomUUID() + ".md";
    }

    private String generateImageFileName(String originalFilename) {
        return UUID.randomUUID() + "_" + originalFilename;
    }

}
