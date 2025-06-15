package com.seaumsiddiqui.personalblog.service;

import com.oracle.bmc.objectstorage.requests.DeleteObjectRequest;
import com.oracle.bmc.objectstorage.requests.PutObjectRequest;
import com.seaumsiddiqui.personalblog.storage.OracleClientConfiguration;
import com.seaumsiddiqui.personalblog.storage.CloudConfigurationProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

@RequiredArgsConstructor
@Service
public class ObjectStorageService {
    private final CloudConfigurationProperties cloudConfiguration;
    private final OracleClientConfiguration clientConfiguration;


    public String uploadMarkdownContent(String content) {
        String objectName = UUID.randomUUID() + ".md";
        return uploadFile(content, objectName);
    }

    public void updateMarkdownContent(String objectUrl, String content) {
        String objectName = stripObjectName(objectUrl);
        uploadFile(content, objectName);
    }

    private String uploadFile(String content, String objectName) {
        try(InputStream inputStream = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8))) {

            // build upload request for a markdown file
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .namespaceName(cloudConfiguration.getNamespace())
                    .bucketName(cloudConfiguration.getBucketName())
                    .objectName(objectName)
                    .putObjectBody(inputStream)
                    .contentType("text/markdown")
                    .contentLength((long) content.getBytes(StandardCharsets.UTF_8).length)
                    .build();

            // upload markdown file
            clientConfiguration.getObjectStorage().putObject(putObjectRequest);
        } catch (IOException e) {
            throw new RuntimeException("Error uploading markdown file: ", e);
        }
        return createObjectUrl(objectName);
    }

    public String uploadImage(MultipartFile file) {
        String objectName = UUID.randomUUID() + "_" + file.getOriginalFilename();

        try(InputStream inputStream = file.getInputStream()) {

            // build upload request for a multipart file
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .namespaceName(cloudConfiguration.getNamespace())
                    .bucketName(cloudConfiguration.getBucketName())
                    .objectName(objectName)
                    .putObjectBody(inputStream)
                    .contentType(file.getContentType())
                    .contentLength(file.getSize())
                    .build();

            // upload image
            clientConfiguration.getObjectStorage().putObject(putObjectRequest);
        } catch (IOException e) {
            throw new RuntimeException("Error uploading image: ", e);
        }
        return createObjectUrl(objectName);
    }


    private String createObjectUrl(String objectName) {
        return String.format("https://objectstorage.%s.oraclecloud.com/n/%s/b/%s/o/%s",
                cloudConfiguration.getRegion(),
                cloudConfiguration.getNamespace(),
                cloudConfiguration.getBucketName(),
                URLEncoder.encode(objectName, StandardCharsets.UTF_8));
    }

    public void deleteFileObject(String objectUrl) {
        String objectName = stripObjectName(objectUrl);

        // build delete request
        DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder()
                .namespaceName(cloudConfiguration.getNamespace())
                .bucketName(cloudConfiguration.getBucketName())
                .objectName(objectName)
                .build();

        try {
            // delete object
            clientConfiguration.getObjectStorage().deleteObject(deleteObjectRequest);
        } catch (IOException e) {
            throw new RuntimeException("Failed to delete object from OCI bucket: " + e.getMessage());
        }
    }

    private String stripObjectName(String objectUrl) {
        try {
            String[] section = objectUrl.split("/o/");
            if (section.length == 2) {
                return URLDecoder.decode(section[1], StandardCharsets.UTF_8);
            } else {
                throw new IllegalArgumentException("Invalid URL format, unable to extract object name.");
            }
        } catch (Exception e) {
            throw new RuntimeException("Error decoding object name from the URL: " + e);
        }
    }

}
