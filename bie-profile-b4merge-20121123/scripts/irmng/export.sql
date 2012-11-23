-- Export IRMNG data from MySQL DB irmng20111014 on diasbtest1-cbr.vm.csiro.au
--
-- IRMNG Family extant and habitat data
-- NC 2012-01-25 : Change the scripts for the new data. Also reenabled the export of fossil records.

select FAMILY_ID, FAMILY, KINGDOM, FAM_EXTANT_FLAG, FAM_HABITAT_FLAG, IS_EXTANT, IS_RECENTLY_EXTINCT, IS_FOSSIL, IS_MARINE, IS_BRACKISH, IS_FRESHWATER, IS_TERRESTRIAL 
from MASTER_FAMLIST
-- where FAM_EXTANT_FLAG != 'F'
order by family
into outfile '/data/bie-staging/irmng/family_list.txt'
fields enclosed by '"';

-- IRMNG Genus extant and habitat data

select GENUS_ID, GENUS, FAMILY, IRMNG_HIERARCHY, GEN_EXTANT_FLAG, GEN_HABITAT_FLAG, IS_EXTANT, IS_RECENTLY_EXTINCT, IS_FOSSIL, IS_MARINE, IS_BRACKISH, IS_FRESHWATER, IS_TERRESTRIAL 
from MASTER_GENLIST 
-- where GEN_EXTANT_FLAG <>'F'
order by genus
into outfile '/data/bie-staging/irmng/genus_list.txt'
fields enclosed by '"';

-- IRMNG Species extant and habitat data

select sp.SPECIES_ID, concat(g.GENUS, ' ', sp.SPECIES), g.GENUS, g.IRMNG_HIERARCHY + '-' + g.FAMILY, sp_extant_flag, sp_habitat_flag,
sp.IS_EXTANT, sp.IS_RECENTLY_EXTINCT, sp.IS_FOSSIL, sp.IS_MARINE, sp.IS_BRACKISH, sp.IS_FRESHWATER, sp.IS_TERRESTRIAL
from MASTER_SPLIST sp join  MASTER_GENLIST g on sp.GENUS_ID=g.GENUS_ID
-- where sp.SP_EXTANT_FLAG != 'F' 
order by g.genus, species
into outfile '/data/bie-staging/irmng/species_list.txt'
fields enclosed by '"';