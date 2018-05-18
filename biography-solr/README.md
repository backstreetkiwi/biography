Build dockerfile:

(execute in folder biography-solr)

```
docker build -t de.zaunkoenigweg/biography-solr:latest .
```

Start container:

```
docker run --name biography_solr -d -p 8983:8983 -t de.zaunkoenigweg/biography-solr:latest
```

Create empty core:

```
docker exec -it --user=solr biography_solr bin/solr create_core -c biography -d /opt/solr/temp/biography/
```

Stop and remove container:

```
docker rm -f biography_solr
```
