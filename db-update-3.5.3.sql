-- https://github.com/AtlasOfLivingAustralia/specieslist-webapp/issues/137
-- List type "Local checklist" should be renamed "Species checklist"

UPDATE specieslist.species_list
SET list_type = 'SPECIES_LIST'
WHERE list_type = 'LOCAL_LIST';

