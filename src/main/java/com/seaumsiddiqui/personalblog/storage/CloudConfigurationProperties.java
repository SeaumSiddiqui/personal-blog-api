package com.seaumsiddiqui.personalblog.storage;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;


@Data
@Configuration
@ConfigurationProperties(prefix = "oracle.cloud")
public class CloudConfigurationProperties {
    private String region;
    private String namespace;
    private String bucketName;

}

