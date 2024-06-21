# Species lists and traits   [![Build Status](https://travis-ci.org/AtlasOfLivingAustralia/specieslist-webapp.svg?branch=master)](https://travis-ci.org/AtlasOfLivingAustralia/specieslist-webapp)

This is a  grails application for managing species lists. It supports:

 * Upload of species profile data in CSV format
 * UI for viewing and faceting species
 * Supports any number of key/value pairs
 * JSON services used by
   * BIE - species pages (https://bie.ala.org.au)
   * Biocache - indexing occurrences (https://biocache.ala.org.au)
 * Supports single owner & multiple editors of a single list
 * Lists can be tagged as:
   * authoritative
   * conservation
   * sensitive
 * Supports sensitive species lists
 
### Architecture 

 * Grails web application
 * MySQL database

### Installation

There is an Ansible playbook for this application here [ala-install/ansible/species-list-standalone.yml](https://github.com/AtlasOfLivingAustralia/ala-install/blob/master/ansible/species-list-standalone.yml)
See example inventory
[ala-install/ansible/inventories/vagrant/species-list-vagrant](https://github.com/AtlasOfLivingAustralia/ala-install/tree/master/ansible/inventories/vagrant)

### Setting up for development

This application only needs a running MySQL instance and the app itself.
There is a docker compose file that can be used to run a local MySQL instance.
To use,  install docker and docker compose and run:

```$xslt
docker-compose -f mysql.yml up
```

This should setup a running MySQL instance and expose port 3306. This is for development purposes only.
Once the docker instance is running, the app can be started using 

```$xslt
grails run-app
```


### Changelog
- **Version 4.2.0**:
  - improve the performance of rematching processing
- **Version 3.0**:
  - Grails 3, Lucene 5 or above
- **Version 2.0**:
  - upgraded to Grails 3
- **Version 1.0**:
  - upgraded to asset pipeline and grails 2.5.6
  - fixed unit and integration tests

### Release
- **Version 4.2.0**:
  Database change:
    -  rematch_Log table changed:
  
  ```
  DROP TABLE IF EXISTS `rematch_log`;
  
  CREATE TABLE `rematch_log` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `version` bigint NOT NULL,
  `end_time` datetime(6) DEFAULT NULL,
  `processing` varchar(255) DEFAULT NULL,
  `start_time` datetime(6) NOT NULL,
  `by_whom` varchar(255) NOT NULL,
  `status` varchar(255) DEFAULT NULL,
  `latest_processing_time` datetime(6) DEFAULT NULL,
  `history` longtext,
  PRIMARY KEY (`id`)
  ) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4;
  