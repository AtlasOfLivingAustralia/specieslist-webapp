# specieslist-webapp   [![Build Status](https://travis-ci.org/AtlasOfLivingAustralia/specieslist-webapp.svg?branch=master)](https://travis-ci.org/AtlasOfLivingAustralia/specieslist-webapp)

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

 
