package com.github.hronom.solrcloudplaygroundcollapsing;

import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.CloudSolrClient;
import org.apache.solr.client.solrj.request.CollectionAdminRequest;
import org.apache.solr.client.solrj.request.UpdateRequest;
import org.apache.solr.client.solrj.response.CollectionAdminResponse;
import org.apache.solr.client.solrj.response.SolrResponseBase;
import org.apache.solr.client.solrj.response.UpdateResponse;
import org.apache.solr.common.SolrInputDocument;
import org.apache.solr.common.cloud.SolrZkClient;
import org.apache.solr.common.cloud.ZkConfigManager;
import org.apache.solr.common.params.UpdateParams;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;

import static java.util.Arrays.asList;

public class SolrCloudPlaygroundCollapsingApp {
    private static final String COLLECTION_NAME = "test";
    private static final String CONFIG_NAME = "test";
    private static final String[] JOB_TYPES = new String[]{"developer", "digger", "runner", "ninja", "thinker"};

    public static void main(String[] args) throws IOException, SolrServerException {
        try (CloudSolrClient cloudSolrClient =
                     new CloudSolrClient
                             .Builder(asList("localhost:2181", "localhost:2182", "localhost:2183"), Optional.of("/"))
                             .sendDirectUpdatesToShardLeadersOnly()
                             .build()) {

            String zkServerAddress = cloudSolrClient.getZkHost();
            try (SolrZkClient solrZkClient = new SolrZkClient(zkServerAddress, 10000)) {
                uploadConfig(solrZkClient);
                createCollection(cloudSolrClient);
                uploadDocumentsToCollection(cloudSolrClient);
            }
        }
    }

    public static void uploadConfig(SolrZkClient solrZkClient) throws IOException {
        System.out.println("Uploading configs...");
        ZkConfigManager zkConfigManager = new ZkConfigManager(solrZkClient);
        zkConfigManager.uploadConfigDir(Paths.get("solr", "configs"), CONFIG_NAME);
        System.out.println("Uploading configs done.");
    }

    public static void createCollection(CloudSolrClient cloudSolrClient) throws IOException, SolrServerException {
        System.out.println("Creating collection...");
        CollectionAdminRequest.Create createCollectionSolrRequest = CollectionAdminRequest.createCollection(
                COLLECTION_NAME,
                CONFIG_NAME,
                3,
                1
        );
        createCollectionSolrRequest.setRouterName("compositeId");
        createCollectionSolrRequest.setRouterField("user_id");
        CollectionAdminResponse response = createCollectionSolrRequest.process(cloudSolrClient, COLLECTION_NAME);
        checkSolrResponse(response);
        System.out.println("Creating collection done.");
    }

    public static void uploadDocumentsToCollection(CloudSolrClient cloudSolrClient) throws IOException, SolrServerException {
        System.out.println("Upload documents to collection...");
        for (int i = 0; i < 10_000; i++) {
            SolrInputDocument solrInputDocument = new SolrInputDocument();
            solrInputDocument.setField("id", String.valueOf(i));
            solrInputDocument.setField("user_id", String.valueOf(ThreadLocalRandom.current().nextInt(1000)));
            solrInputDocument.setField("job_type", JOB_TYPES[ThreadLocalRandom.current().nextInt(JOB_TYPES.length)]);

            UpdateRequest req = new UpdateRequest();
            req.add(solrInputDocument);
            UpdateResponse updateResponse = req.process(cloudSolrClient, COLLECTION_NAME);
            checkSolrResponse(updateResponse);
        }

        UpdateRequest req = new UpdateRequest();
        req.setParam(UpdateParams.COMMIT, "true");
        req.setWaitSearcher(true);
        UpdateResponse updateResponse = req.process(cloudSolrClient, COLLECTION_NAME);
        checkSolrResponse(updateResponse);

        System.out.println("Upload documents to collection done.");
    }

    private static void checkSolrResponse(SolrResponseBase response) {
        if (response.getStatus() != 0) {
            throw new IllegalStateException("Solr response has error code "+ response.getStatus());
        }
    }
}
