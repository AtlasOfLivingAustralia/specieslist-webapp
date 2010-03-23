-- export of common names, that works against CoL 2009

select cn.common_name, t.name, f.kingdom from common_names cn 
inner join taxa t on t.name_code=cn.name_code 
inner join scientific_names s on s.name_code=cn.name_code 
inner join families f on s.family_id=f.record_id 
where cn.language='English' group by cn.common_name, t.name, f.kingdom
into outfile '/data/bie-staging/col/commonNames.txt';

select f.family_common_name, f.family, f.kingdom from families f
where f.family_common_name is not null
group by f.family_common_name, f.family, f.kingdom
into outfile '/data/bie-staging/col/familyCommonNames.txt';