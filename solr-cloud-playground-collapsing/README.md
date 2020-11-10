# solr-cloud-basic-auth

### 1. Launch Solr Cloud
```
docker-compsoe -f solr-cluster.yml up -d
```

### 2. Launch app
```
com.github.hronom.solrcloudplaygroundcollapsing.SolrCloudPlaygroundCollapsingApp
```

### 3. Try to execute query in Admin UI
```
curl --location --request GET 'http://localhost:8981/solr/test/select?facet.field={!ex=selected}job_type&facet=on&fq={!collapse%20field=user_id}&fq={!tag=selected}job_type:thinker&q=*:*&rows=0'
```