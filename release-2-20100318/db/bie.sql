drop database if exists bie;
create database bie CHARACTER SET = UTF8;
use bie;
-- DROP TABLE IF EXISTS infosource;
create table infosource (
  id int unsigned not null auto_increment,
  acronym varchar(255),
  name varchar(255) not null,
  description TEXT,
  uri varchar(255),
  logo_url varchar(255),
  website_url varchar(255),
  dataset_type int unsigned,
  states varchar(255),  -- split out
  geographic_description TEXT,  -- split out
  well_known_text TEXT,  -- split out
  north_coordinate FLOAT DEFAULT NULL,  -- split out
  south_coordinate FLOAT DEFAULT NULL,  -- split out
  east_coordinate FLOAT DEFAULT NULL,  -- split out
  west_coordinate FLOAT DEFAULT NULL,  -- split out
  single_date DATE DEFAULT NULL,  -- split out
  start_date DATE DEFAULT NULL,  -- split out
  end_date DATE DEFAULT NULL,  -- split out
  scientific_names TEXT,  -- split out  
  connection_params TEXT, -- JSON
  harvester_id MEDIUMINT unsigned DEFAULT NULL,
  document_mapper varchar(255),
  primary key (id)
) ENGINE=MyISAM DEFAULT CHARSET=UTF8;

create table document (
  id int unsigned not null auto_increment,
  parent_document_id int unsigned,
  infosource_id int unsigned not null,
  uri varchar(255) not null,
  mime_type varchar(255) not null,
  file_path varchar(255) null,
  created varchar(255) not null,
  modified varchar(255) not null,
  primary key (id)
) ENGINE=MyISAM DEFAULT CHARSET=UTF8;
alter table document add index ix_uri (uri);
alter table document add index ix_doc_info(infosource_id);

create table harvester (
  id MEDIUMINT unsigned not null auto_increment,
  name varchar(255) not null,
  class varchar(255) not null,
  primary key (id)
) ENGINE=MyISAM DEFAULT CHARSET=UTF8;

-- create table documentmapper (
--  id int unsigned not null auto_increment,
--  class varchar(255) not null,
--  primary key (id)
-- ) ENGINE=MyISAM DEFAULT CHARSET=UTF8;

-- DROP TABLE IF EXISTS  predicate, vocabulary, term, term_mapping;

create table predicate (
  id int unsigned,
  predicate varchar(255) not null,
  primary key (id)
) ENGINE=MyISAM DEFAULT CHARSET=UTF8;

create table vocabulary (
  id int unsigned not null auto_increment,
  uri varchar(255),
  name varchar(255) not null,
  description varchar(255) not null,
  infosource_id int unsigned,
  predicate_id int not null,
  primary key (id)
) ENGINE=MyISAM DEFAULT CHARSET=UTF8;

create table term (
  id int unsigned not null auto_increment,
  vocabulary_id int unsigned not null,
  term_string varchar(255) not null,
  uri varchar(255),
  primary key (id,vocabulary_id)
) ENGINE=MyISAM DEFAULT CHARSET=UTF8;

create table term_mapping (
  source_term_id int,
  target_term_id int, 
  primary key (source_term_id,target_term_id)
) ENGINE=MyISAM DEFAULT CHARSET=UTF8;