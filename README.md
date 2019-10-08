# Species lists and traits   [![Build Status](https://travis-ci.org/AtlasOfLivingAustralia/specieslist-webapp.svg?branch=master)](https://travis-ci.org/AtlasOfLivingAustralia/specieslist-webapp)

This is a  grails application for managing species lists. It supports:

 * Upload of species profile data in CSV format
 * UI for viewing and faceting species
 * Supports any number of key/value pairs
 * JSON services used by
   * BIE - species pages
   * Biocache - indexing occurrences
 * Supports single owner & multiple editors of a single list
 * Lists can be tagged as:
   * authoritative
   * conservation
 * Supports sensitive species lists

 
### Architecture 

 * Grails web application
 * MySQL database


### Installation

There is an Ansible playbook for this application here [ala-install/ansible/species-list-standalone.yml](https://github.com/AtlasOfLivingAustralia/ala-install/blob/master/ansible/species-list-standalone.yml)
See example inventory
[ala-install/ansible/inventories/vagrant/species-list-vagrant](https://github.com/AtlasOfLivingAustralia/ala-install/tree/master/ansible/inventories/vagrant)


### Setting up for development

There is a docker compose file that can be used to run a local MySQL instance.
To use,  install docker and docker compose and run:

```$xslt
docker-compose -f mysql.yml up
```

This should setup a running MySQL instance and expose port 3306. This is for development purposes only.


### Changelog
- **Version 3.0**:
  - Grails 3, Lucene 5 or above
- **Version 2.0**:
  - upgraded to Grails 3
- **Version 1.0**:
  - upgraded to asset pipeline and grails 2.5.6
  - fixed unit and integration tests
