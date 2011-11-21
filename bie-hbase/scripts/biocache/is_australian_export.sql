DELIMITER $$

-- Stored function to determine whether or not the supplied
-- taxon concept is in australia.

-- Natasha Carter 2010-08-18
-- Args:
-- concept_id: the input concept id
DROP FUNCTION IF EXISTS is_australian_concept$$
CREATE FUNCTION is_australian_concept(concept_id INT) returns BOOLEAN
DETERMINISTIC
READS SQL DATA
    BEGIN
        DECLARE is_aust BOOLEAN;
		select count(*)>0 
		INTO is_aust
		FROM occurrence_record oc 
		JOIN geo_mapping gm ON oc.id = gm.occurrence_id 
		JOIN geo_region gr on gm.geo_region_id = gr.id 
		WHERE oc.taxon_concept_id = concept_id and gr.region_type<=11;
		
        RETURN is_aust;
    END $$

-- Stored function to determine whether or not the specified concept
-- has Australian chidlren

-- Natasha Carter 2010-08-19
-- Args:
-- concept_id the input concept id
DROP FUNCTION IF EXISTS has_aust_child$$
CREATE FUNCTION has_aust_child(concept_id INT) returns BOOLEAN
DETERMINISTIC
READS SQL DATA
	BEGIN
		DECLARE has_aust BOOLEAN;
		SELECT count(*)>0
		INTO has_aust
		FROM taxon_concept tc1
		JOIN taxon_concept tc2 ON tc1.lft BETWEEN tc2.lft AND tc2.rgt
		WHERE tc1.is_aust AND tc1.priority =20 AND tc2.id = concept_id;
		
		RETURN has_aust;
	END $$
	
DELIMITER ;

-- update the taxon concept table where
update taxon_concept set is_aust = is_australian_concept(id) where not is_aust;

-- update the parent taxon concept as australian up to the family level
update taxon_concept set is_aust = has_aust_child(id) where is_aust = false and rank >=5000 and priority =20;

-- dump the "Australian" taxon concepts to a file from family level down
select guid into outfile '/data/bie-staging/biocache/austTaxonConcepts.txt' 
from taxon_concept 
where is_aust and priority = 20 and rank >=5000;