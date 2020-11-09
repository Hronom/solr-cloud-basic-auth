# solr-cloud-basic-auth

### 1. Launch Solr Cloud
```
docker-compsoe -f solr-cluster.yml up -d
```

### 2. Launch app
```
com.github.hronom.solrcloudbasicauth.App.main
```

### 3. Create collection in Admin UI based on `test` config-set

### 4. Try to execute query in Admin UI