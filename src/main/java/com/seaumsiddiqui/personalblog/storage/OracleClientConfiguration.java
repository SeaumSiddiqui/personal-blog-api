package com.seaumsiddiqui.personalblog.storage;

import com.oracle.bmc.ConfigFileReader;
import com.oracle.bmc.auth.ConfigFileAuthenticationDetailsProvider;
import com.oracle.bmc.objectstorage.ObjectStorage;
import com.oracle.bmc.objectstorage.ObjectStorageClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;

@Configuration
public class OracleClientConfiguration {
    // OCI Config file path in local machine
    @Value("${oracle.config.path}")
    String OCI_CONFIG_PATH;
    String profile = "DEFAULT";

    public ObjectStorage getObjectStorage() throws IOException {

        // load config file
        final ConfigFileReader.ConfigFile configFile = ConfigFileReader.parse(OCI_CONFIG_PATH, profile);
        ConfigFileAuthenticationDetailsProvider provider = new ConfigFileAuthenticationDetailsProvider(configFile);

        // build & return client
        return ObjectStorageClient.builder().build(provider);
    }
}
