package com.github.hronom.solrcloudbasicauth;

import org.apache.solr.client.solrj.impl.CloudSolrClient;
import org.apache.solr.common.cloud.SolrZkClient;
import org.apache.solr.common.cloud.ZkConfigManager;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.Optional;

import static java.util.Arrays.asList;
import static org.apache.solr.common.cloud.ZkConfigManager.UPLOAD_FILENAME_EXCLUDE_PATTERN;

public class App {
    public static void main(String[] args) throws IOException {
        try (CloudSolrClient cloudSolrClient =
                     new CloudSolrClient
                             .Builder(asList("localhost:2181", "localhost:2182", "localhost:2183"), Optional.of("/"))
                             .sendDirectUpdatesToShardLeadersOnly()
                             .build()) {

            String zkServerAddress = cloudSolrClient.getZkHost();
            try (SolrZkClient solrZkClient = new SolrZkClient(zkServerAddress, 10000)) {
                uploadConfig(solrZkClient);
                uploadSecurityConfig(solrZkClient);
            }
        }
    }

    public static void uploadConfig(SolrZkClient solrZkClient) throws IOException {
        System.out.println("Uploading configs...");
        ZkConfigManager zkConfigManager = new ZkConfigManager(solrZkClient);
        zkConfigManager.uploadConfigDir(Paths.get("solr", "configs"), "test");
        System.out.println("Uploading configs done.");
    }

    public static void uploadSecurityConfig(SolrZkClient solrZkClient) throws IOException {
        System.out.println("Uploading security config...");
        solrZkClient.uploadToZK(Paths.get("solr", "security"), "/", UPLOAD_FILENAME_EXCLUDE_PATTERN);
        System.out.println("Uploading security config done.");
    }
}
