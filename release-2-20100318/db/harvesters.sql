use bie;
-- taxonomic sources
INSERT INTO harvester (id, name, class) VALUES (1, 'CSV Harvester', 'org.ala.harvester.CSVHarvester');
INSERT INTO harvester (id, name, class) VALUES (2, 'Flickr Harvester', 'org.ala.harvester.FlickrHarvester');
-- INSERT INTO harvester (id, class) VALUES (3, 'org.ala.harvester.RDFExportHarvester');