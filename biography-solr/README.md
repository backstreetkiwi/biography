Build dockerfile:

(execute in folder biography-solr)

Builds an image which contains the config template for a biography Solr core.

```
docker build -t de.zaunkoenigweg/biography-solr:latest .
```

Start container and create core if not existing:

The core is stored in an external volume to keep it even if the container is stopped/removed.
If the config has changed, just remove the core in the mapped host dir and it gets created.

```
docker run --name biography_solr -it --rm -p 8983:8983 -v <your local dir>:/opt/solr/server/solr/mycores de.zaunkoenigweg/biography-solr:latest solr-precreate biography /opt/solr/temp/biography/
```

Stop and remove container:

```
docker rm -f biography_solr
```
