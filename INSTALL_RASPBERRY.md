# Install Biography on a Raspberry PI

## Prepare Raspberry PI

- Format SD with exactly one `fat32` Partition (Ubuntu: `gparted`)
- Download [NOOBS](https://www.raspberrypi.org/downloads/noobs/) and unzip it onto the SD
- Connect PI to screen, keyboard and mouse, insert SD
- Boot PI and follow the instructions (install Raspbian)
- Once the system is up and running, some settings should be made:
  - rename the PI to `biography` (e.g.)
  - let the PI boot to command line instead of GUI
  - allow SSHing into it
  - change password of default user `pi`

## Install and configure Solr

- [Download](http://lucene.apache.org/solr/downloads.html) Solr as ZIP (recommended: Version 7.4.0)
- create dir `/home/pi/biography/solr`
- upload ZIP there using `scp`
- in `/home/pi/biography/solr`: `unzip solr`...
- in `/home/pi/biography/solr`: `mkdir config-templates`...
- upload index config files `biography/biography-solr/conf/*.xml` to `...config-templates` using `scp`
- Start Solr: `./solr start`
- Create core/index: `./solr create_core -c biography -d /home/pi/biography/solr/config-templates/``
- (To stop Solr: `./solr stop`)
- Admin UI for Solr is available via Web Browser: http://biography:8983


## Install and configure biography

- Create installation dir: `/home/pi/biography`
- Create binary dir: `/home/pi/biography/bin`
- upload JAR to binary dir using scp: `biography-web-<version>.jar`
- Create import dir: `/home/pi/biography/data/import`
- Create archive dir: `/home/pi/biography/data/archive`
- Copy existing archive into archive dir
- Create config file `/home/pi/biography/biograpyh.yml`

      spring:
        resources:
          static-locations: classpath:/static/
        servlet:
          multipart:
            max-file-size: "1024MB"
            max-request-size: "1024MB"

      server:
        port: 8080

      # or use mounted HDD instead
      archive:
        path: /home/pi/biography/data/archive

      import:
        path: /home/pi/biography/data/import/

      solr:
        index:
        url: http://localhost:8983/solr/biography
