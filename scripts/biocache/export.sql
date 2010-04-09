-- Export Occurrend data from MySQL DB portal on alaproddb1-cbr.vm.csiro.au
--
--Species occurrences by region - species id, species name, region, region_type, no of occurrences

select tc.id, tn.canonical, grt.name, gr.id, gr.name, count(*) as occurrences from occurrence_record oc
inner join geo_mapping gm on gm.occurrence_id=oc.id
inner join geo_region gr on gm.geo_region_id=gr.id
inner join geo_region_type grt on gr.region_type=grt.id
inner join taxon_concept tc on tc.id=oc.species_concept_id
inner join taxon_name tn on tn.id=tc.taxon_name_id
group by tc.id, gr.id
order by tn.canonical, grt.id, occurrences desc
into outfile '/data/bie-staging/biocache/species_region.txt'
fields enclosed by '"';


--Genus occurrences by region - genus id, genus name, region, region_type, no of occurrences

select tc.id, tn.canonical, grt.name, gr.id, gr.name, count(*) as occurrences from occurrence_record oc
inner join geo_mapping gm on gm.occurrence_id=oc.id
inner join geo_region gr on gm.geo_region_id=gr.id
inner join geo_region_type grt on gr.region_type=grt.id
inner join taxon_concept tc on tc.id=oc.genus_concept_id
inner join taxon_name tn on tn.id=tc.taxon_name_id
group by tc.id, gr.id
order by tn.canonical, grt.id, occurrences desc
into outfile '/data/bie-staging/biocache/genus_region.txt'
fields enclosed by '"';


--Family occurrences by region - family id, family name, region, region_type, no of occurrences

select tc.id, tn.canonical, grt.name, gr.id, gr.name, count(*) as occurrences from occurrence_record oc
inner join geo_mapping gm on gm.occurrence_id=oc.id
inner join geo_region gr on gm.geo_region_id=gr.id
inner join geo_region_type grt on gr.region_type=grt.id
inner join taxon_concept tc on tc.id=oc.family_concept_id
inner join taxon_name tn on tn.id=tc.taxon_name_id
group by tc.id, gr.id
order by tn.canonical, grt.id, occurrences desc
into outfile '/data/bie-staging/biocache/family_region.txt'
fields enclosed by '"';
