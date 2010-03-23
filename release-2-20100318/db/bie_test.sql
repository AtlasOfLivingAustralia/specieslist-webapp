drop database if exists bie_test;
create database bie_test CHARACTER SET = UTF8;
use bie_test;

create table infosource (
  id int unsigned not null auto_increment,
  name varchar(255) not null,
  uri varchar(255) not null,
  logo_url varchar(255) not null,
  description text,
  connection_params text,
  harvester_id int,
  document_mapper varchar(255),
  primary key (id)
) ENGINE=MyISAM DEFAULT CHARSET=UTF8;

create table harvester (
  id int unsigned not null auto_increment,
  class varchar(255) not null,
  primary key (id)
) ENGINE=MyISAM DEFAULT CHARSET=UTF8;

--create table documentmapper (
--  id int unsigned not null auto_increment,
--  class varchar(255) not null,
--  primary key (id)
--) ENGINE=MyISAM DEFAULT CHARSET=UTF8;

create table vocabulary (
  id int unsigned not null auto_increment,
  uri varchar(255) not null,
  name varchar(255) not null,
  description varchar(255) not null,
  primary key (id)
) ENGINE=MyISAM DEFAULT CHARSET=UTF8;

create table term (
  id int unsigned not null auto_increment,
  vocabulary_id int unsigned not null,
  term_string varchar(255) not null,
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
