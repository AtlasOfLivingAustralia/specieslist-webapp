use bie;

-- ============================== --
-- Predicates to map
-- ============================== --

set @hasPestStatus=1;
set @hasConservationStatus=2;
set @hasRegion=3;
set @hasSystem=4;

insert into predicate (id, predicate) values 
(@hasPestStatus, 'hasPestStatus'),
(@hasConservationStatus, 'hasConservationStatus'),
(@hasRegion, 'hasRegion'),
(@hasSystem, 'hasSystem');


-- ********************************************************************************************* --
-- Preferred vocabularies
-- ********************************************************************************************* --


-- ============================== --
-- PEST STATUS
-- ============================== --

set @pest_voc_id=1;

INSERT INTO vocabulary (id, uri, name, description, predicate_id) values 
(@pest_voc_id, "http://vocabularies.gbif.org/vocabularies/nativeness", "Nativeness", "GBIF vocabulary for nativeness", @hasPestStatus);

INSERT INTO term (id,vocabulary_id,term_string) VALUES 
(1, @pest_voc_id, "introduced"),
(2, @pest_voc_id, "invasive"),
(3, @pest_voc_id, "managed"),
(4, @pest_voc_id, "native"),
(5, @pest_voc_id, "naturalised"),
(6, @pest_voc_id, "unknown");

-- ============================== --
-- CONSERVATION STATUS
-- ============================== --

set @conservation_voc_id=2;

INSERT INTO vocabulary (id, uri, name, description, predicate_id) values 
(@conservation_voc_id, "http://vocabularies.gbif.org/vocabularies/threat_stat_iucn", "Threat Status IUCN", "GBIF vocabulary for Threat Status IUCN", @hasConservationStatus);

INSERT INTO term (id,vocabulary_id,term_string) VALUES 
(1001, @conservation_voc_id, "Extinct"),
(1002, @conservation_voc_id, "Extinct in the Wild"),
(1003, @conservation_voc_id, "Critically Endangered"),
(1004, @conservation_voc_id, "Endangered"),
(1005, @conservation_voc_id, "Vulnerable"),
(1006, @conservation_voc_id, "Near Threatened"),
(1007, @conservation_voc_id, "Least Concern"),
(1008, @conservation_voc_id, "Data Deficient"),
(1009, @conservation_voc_id, "Not Evaluated");

-- ============================== --
-- AUSTRALIAN STATES
-- ============================== --

set @states_voc_id=3;

INSERT INTO vocabulary (id, uri, name, description, predicate_id) values 
(@states_voc_id, "http://ala.org.au/region/states", "Australian States and Territories", "Vocabulary of Australian States", @hasRegion);

INSERT INTO term (id,vocabulary_id,term_string) VALUES 
(2001, @states_voc_id, "Australian Capital Territory"),
(2002, @states_voc_id, "Northern Territory"),
(2003, @states_voc_id, "Western Australia"),
(2004, @states_voc_id, "Queensland"),
(2005, @states_voc_id, "Victoria"),
(2006, @states_voc_id, "New South Wales"),
(2007, @states_voc_id, "Tasmania"),
(2008, @states_voc_id, "South Australia");


-- ********************************************************************************************* --
-- Raw sourced vocabularies
-- ********************************************************************************************* --


-- ============================== --
-- PADIL
-- ============================== --

set @padil_id=1023;
set @padil_pest_voc_id=10000;

INSERT INTO vocabulary (id, uri, name, description, infosource_id, predicate_id) values 
(@padil_pest_voc_id, "http://ala.org.au/voc/padil", "PaDIL Species Pest Status", "Pest Status values used on PaDIL species pages", @padil_id, @hasPestStatus);

-- insert padil pest terms
INSERT INTO term (id,vocabulary_id,term_string) VALUES (10001,@padil_pest_voc_id, "Biological Control Agent");
INSERT INTO term (id,vocabulary_id,term_string) VALUES (10002,@padil_pest_voc_id, "Exotic (absent from Australia)");
INSERT INTO term (id,vocabulary_id,term_string) VALUES (10003,@padil_pest_voc_id, "Exotic (absent from Australia) Beneficial Species");
INSERT INTO term (id,vocabulary_id,term_string) VALUES (10004,@padil_pest_voc_id, "Exotic (absent from Australia) High Impact Pest Species");
INSERT INTO term (id,vocabulary_id,term_string) VALUES (10005,@padil_pest_voc_id, "Exotic (absent from Australia) Pest Species");
INSERT INTO term (id,vocabulary_id,term_string) VALUES (10006,@padil_pest_voc_id, "Exotic (but present in Australia)");
INSERT INTO term (id,vocabulary_id,term_string) VALUES (10007,@padil_pest_voc_id, "Exotic (established in Australia)");
INSERT INTO term (id,vocabulary_id,term_string) VALUES (10008,@padil_pest_voc_id, "Exotic (established in Australia) Beneficial Species");
INSERT INTO term (id,vocabulary_id,term_string) VALUES (10009,@padil_pest_voc_id, "Exotic (established in Australia) High Impact Pest Species");
INSERT INTO term (id,vocabulary_id,term_string) VALUES (10010,@padil_pest_voc_id, "Exotic (established in Australia) Pest Species");
INSERT INTO term (id,vocabulary_id,term_string) VALUES (10011,@padil_pest_voc_id, "Exotic (intercepted in Australia)");
INSERT INTO term (id,vocabulary_id,term_string) VALUES (10012,@padil_pest_voc_id, "Exotic (intercepted in Australia) Beneficial Species");
INSERT INTO term (id,vocabulary_id,term_string) VALUES (10013,@padil_pest_voc_id, "Exotic (intercepted in Australia) High Impact Pest Species");
INSERT INTO term (id,vocabulary_id,term_string) VALUES (10014,@padil_pest_voc_id, "Exotic (intercepted in Australia) Pest Species");
INSERT INTO term (id,vocabulary_id,term_string) VALUES (10015,@padil_pest_voc_id, "Exotic (present in Australia)");
INSERT INTO term (id,vocabulary_id,term_string) VALUES (10016,@padil_pest_voc_id, "Exotic (present in Australia) Beneficial Species");
INSERT INTO term (id,vocabulary_id,term_string) VALUES (10017,@padil_pest_voc_id, "Exotic (present in Australia) High Impact Pest Species");
INSERT INTO term (id,vocabulary_id,term_string) VALUES (10018,@padil_pest_voc_id, "Exotic (present in Australia) Pest Species");
INSERT INTO term (id,vocabulary_id,term_string) VALUES (10019,@padil_pest_voc_id, "Exotic to Barrow Island, Exotic to Mainland");
INSERT INTO term (id,vocabulary_id,term_string) VALUES (10020,@padil_pest_voc_id, "Exotic to Barrow Island, Native to Mainland");
INSERT INTO term (id,vocabulary_id,term_string) VALUES (10021,@padil_pest_voc_id, "Native to Australia");
INSERT INTO term (id,vocabulary_id,term_string) VALUES (10022,@padil_pest_voc_id, "Native to Australia Beneficial Species");
INSERT INTO term (id,vocabulary_id,term_string) VALUES (10023,@padil_pest_voc_id, "Native to Australia High Impact Pest Species");
INSERT INTO term (id,vocabulary_id,term_string) VALUES (10024,@padil_pest_voc_id, "Native to Australia Pest Species");
INSERT INTO term (id,vocabulary_id,term_string) VALUES (10025,@padil_pest_voc_id, "Native to Barrow Island");
INSERT INTO term (id,vocabulary_id,term_string) VALUES (10026,@padil_pest_voc_id, "Non Indigenous Species to Barrow Island");
INSERT INTO term (id,vocabulary_id,term_string) VALUES (10027,@padil_pest_voc_id, "Present on Barrow Island");
INSERT INTO term (id,vocabulary_id,term_string) VALUES (10028,@padil_pest_voc_id, "Uncertain Status");

insert into term_mapping (source_term_id, target_term_id) values
(10001,3),
(10002,2),
(10003,1),
(10004,2),
(10005,2),
(10006,2),
(10007,2),
(10008,1),
(10009,2),
(10010,2),
(10011,3),
(10012,5),
(10013,2),
(10014,2),
(10015,2),
(10016,2),
(10017,2),
(10018,2),
(10019,2),
(10020,2),
(10021,4),
(10022,4),
(10023,4),
(10024,4),
(10025,4),
(10026,4),
(10027,4),
(10028,6);


-- ============================== --
-- Internet Bird Collection
-- ============================== --

set @ibc_id=1017;
set @ibc_conservation_voc_id=20000;

INSERT INTO vocabulary (id, uri, name, description, infosource_id, predicate_id) values 
(@ibc_conservation_voc_id, "http://ala.org.au/voc/ibc", "Internet Bird Collection Conservation Status", 
"Conservation Status values used on Internet Bird Collection species pages", @ibc_id, @hasConservationStatus);

INSERT INTO term (id,vocabulary_id,term_string) VALUES 
(20001, @ibc_conservation_voc_id, "Extinct"),
(20002, @ibc_conservation_voc_id, "Extinct in the Wild"),
(20003, @ibc_conservation_voc_id, "Critically Endangered"),
(20004, @ibc_conservation_voc_id, "Endangered"),
(20005, @ibc_conservation_voc_id, "Vulnerable"),
(20006, @ibc_conservation_voc_id, "Near Threatened"),
(20007, @ibc_conservation_voc_id, "Least Concern"),
(20008, @ibc_conservation_voc_id, "Data Deficient"),
(20009, @ibc_conservation_voc_id, "Not Evaluated"),
(20010, @ibc_conservation_voc_id, "Extint in the Wild");

-- FIXME - this is using the IUCN vocabulary - need many to many infosource to vocabulary
insert into term_mapping (source_term_id, target_term_id) values
(20001,1001),
(20002,1002),
(20003,1003),
(20004,1004),
(20005,1005),
(20006,1006),
(20007,1007),
(20008,1008),
(20009,1009),
(20010,1002);


-- ============================== --
-- Australian Desert Fishes Descriptions
-- ============================== --

set @adfd_id=1010;
set @adfd_conservation_voc_id=30000;

INSERT INTO vocabulary (id, uri, name, description, infosource_id, predicate_id) values 
(@adfd_conservation_voc_id, "http://ala.org.au/voc/desertfishes", "Australian Desert Fishes Descriptions Conservation Status", 
"Conservation Status values used on Australian Desert Fishes Descriptions species pages", @adfd_id, @hasConservationStatus);

insert into term (id,vocabulary_id,term_string) VALUES 
(30001,@adfd_conservation_voc_id, "None"),
(30002,@adfd_conservation_voc_id, "None."),
(30003,@adfd_conservation_voc_id, "Listed as Threatened by the Australian Society for Fish Biology"),
(30004,@adfd_conservation_voc_id, "Listed as Uncertain Status by the Australian Society for Fish Biology due to a lack of information."),
(30005,@adfd_conservation_voc_id, "Listed as Restricted by the Australian Society for Fish Biology."),
(30006,@adfd_conservation_voc_id, "It is listed as Vulnerable by the Australian Society for Fish Biology."),
(30007,@adfd_conservation_voc_id, "Listed as Vulnerable by the Australian Society for Fish Biology."),
(30008,@adfd_conservation_voc_id, "This species will probably be considered endangered."),
(30009,@adfd_conservation_voc_id, "Not listed."),
(30010,@adfd_conservation_voc_id, "Not listed"),
(30011,@adfd_conservation_voc_id, "Listed as Endangered by the Australian Society for Fish Biology."),
(30012,@adfd_conservation_voc_id, "Not listed. However, if it proves to be a different species it could be listed as Restricted."),
(30013,@adfd_conservation_voc_id, "At present, the Finke goby is unlisted, as it has only recently been recognised as different. It should probably be considered Restricted.");

insert into term_mapping (source_term_id, target_term_id) values
(30001,1009),
(30002,1009),
(30003,1005),
(30004,1009),
(30005,1005),
(30006,1005),
(30007,1005),
(30008,1004),
(30009,1009),
(30010,1009),
(30011,1004),
(30012,1009),
(30013,1009);


-- ============================== --
-- Reptiles Down Under
-- ============================== --

set @rdu_id=1025;
set @rdu_conservation_voc_id=40000;

INSERT INTO vocabulary (id, uri, name, description, infosource_id, predicate_id) values 
(@rdu_conservation_voc_id, "http://ala.org.au/voc/rdu", "Reptiles Down Under Conservation Status", 
"Conservation Status values used on Reptiles Down Under species pages", @rdu_id, @hasConservationStatus);

insert into term (id,vocabulary_id,term_string) VALUES 
(40001,@rdu_conservation_voc_id, "This species does not appear to be listed as of conservation concern."),
(40002,@rdu_conservation_voc_id, "vulnerable"),
(40003,@rdu_conservation_voc_id, "rare or likely to become extinct"),
(40004,@rdu_conservation_voc_id, "specially protected"),
(40005,@rdu_conservation_voc_id, "near threatened"),
(40006,@rdu_conservation_voc_id, "endangered"),
(40007,@rdu_conservation_voc_id, "rare"),
(40008,@rdu_conservation_voc_id, "lower risk near threatened");

insert into term_mapping (source_term_id, target_term_id) values
(40001,1007),
(40002,1005),
(40003,1003),
(40004,1006),
(40005,1006),
(40006,1004),
(40007,1004),
(40008,1006);


-- ============================== --
-- EPBC Act List 
-- ============================== --

set @epbc_id=1011;
set @epbc_conservation_voc_id=50000;

INSERT INTO vocabulary (id, uri, name, description, infosource_id, predicate_id) values 
(@epbc_conservation_voc_id, "http://ala.org.au/voc/epbc", "EPBC Act List of Threatened Conservation Status", 
"Conservation Status values used on EPBC Act List of Threatened species pages", @epbc_id, @hasConservationStatus);

insert into term (id,vocabulary_id,term_string) VALUES 
(50001,@epbc_conservation_voc_id, "Listed as Extinct"),
(50002,@epbc_conservation_voc_id, "Listed migratory"),
(50003,@epbc_conservation_voc_id, "Listed as Extinct in the wild"),
(50004,@epbc_conservation_voc_id, "Listed as Critically Endangered"),
(50005,@epbc_conservation_voc_id, "Listed marine"),
(50006,@epbc_conservation_voc_id, "Listed as Endangered"),
(50007,@epbc_conservation_voc_id, "Listed as Vulnerable"),
(50008,@epbc_conservation_voc_id, "Listed as Conservation Dependent");

insert into term_mapping (source_term_id, target_term_id) values
(50001,1001),
(50003,1002),
(50004,1003),
(50006,1004),
(50007,1005),
(50008,1006);


-- ============================== --
-- Department of Environment and Conservation
-- ============================== --

set @dec_id=1009;
set @dec_conservation_voc_id=60000;

INSERT INTO vocabulary (id, uri, name, description, infosource_id, predicate_id) values 
(@dec_conservation_voc_id, "http://ala.org.au/voc/dec", 
"Department of Environment and Conservation - NSW threatened species Conservation Status", 
"Conservation Status values used on Department of Environment and Conservation - NSW threatened species species pages", 
@dec_id, @hasConservationStatus);

insert into term (id,vocabulary_id,term_string) VALUES 
(60001,@dec_conservation_voc_id, "Conservation status in NSW: Endangered"),
(60002,@dec_conservation_voc_id, "Conservation status in NSW: Vulnerable"),
(60003,@dec_conservation_voc_id, "Conservation status in NSW: Endangered Ecological Community"),
(60004,@dec_conservation_voc_id, "Conservation status in NSW: Endangered Population"),
(60005,@dec_conservation_voc_id, "Conservation status in NSW: Key Threatening Process"),
(60006,@dec_conservation_voc_id, "Conservation status in NSW: Critically Endangered"),
(60007,@dec_conservation_voc_id, "Conservation status in NSW: Not listed"),
(60008,@dec_conservation_voc_id, "Conservation status in NSW: Critically Endangered Ecological Community"),
(60009,@dec_conservation_voc_id, "Conservation status in NSW: Presumed extinct"),
(60010,@dec_conservation_voc_id, "Conservation status in NSW: Preliminary Listing"),
(60011,@dec_conservation_voc_id, "Conservation status in NSW: Vulnerable (Commonwealth listed only)");


insert into term_mapping (source_term_id, target_term_id) values
(60001,1004),(60001,2006),
(60002,1005),(60002,2006),
(60003,1004),(60003,2006),
(60004,1004),(60004,2006),
(60005,1006),(60005,2006),
(60006,1003),(60006,2006),
(60007,1008),(60007,2006),
(60008,1003),(60008,2006),
(60009,1001),(60009,2006),
(60010,1006),(60010,2006),
(60011,1005),(60011,2006);


-- ============================== --
-- Queensland Government
-- ============================== --

set @qge_id=1031;
set @qge_conservation_voc_id=70000;

INSERT INTO vocabulary (id, uri, name, description, infosource_id, predicate_id) values 
(@qge_conservation_voc_id, "http://ala.org.au/voc/qge", 
"Queensland Government - Endangered (Department of Environment and Resource Management) Conservation Status", 
"Conservation Status values used on Queensland Government - Endangered (Department of Environment and Resource Management) species pages", 
@qge_id, @hasConservationStatus);


-- ============================== --
-- NIMPIS
-- ============================== --

set @nimpis_id=1021;
set @nimpis_voc_id=80000;

INSERT INTO vocabulary (id, uri, name, description, infosource_id, predicate_id) values 
(@nimpis_voc_id, "http://ala.org.au/voc/nimpis", 
"NIMPIS Pest Status", 
"Pest Status taken from the NIMPIS web pages", 
@nimpis_id, @hasPestStatus);

insert into term (id,vocabulary_id,term_string) VALUES 
(80001,@nimpis_voc_id, "Known introduction to Australia"),
(80002,@nimpis_voc_id, "Possibly introduced into Australia");

insert into term_mapping (source_term_id, target_term_id) values
(80001,1),
(80002,1);


-- ============================== --
-- FloraBase
-- ============================== --

set @florabase_id=1014;
set @florabase_voc_id=90000;

INSERT INTO vocabulary (id, uri, name, description, infosource_id, predicate_id) values 
(@florabase_voc_id, "http://ala.org.au/voc/florabase", 
"FloraBase conservation status", 
"Conservation status taken from FloraBase website", 
@florabase_id, @hasConservationStatus);

insert into term (id,vocabulary_id,term_string) VALUES 
(90001,@florabase_voc_id, "Alien"),
(90002,@florabase_voc_id, "Declared Rare"),
(90003,@florabase_voc_id, "Not threatened"),
(90004,@florabase_voc_id, "Presumed Extinct"),
(90005,@florabase_voc_id, "Priority One"),
(90006,@florabase_voc_id, "Priority Two"),
(90007,@florabase_voc_id, "Priority Three"),
(90008,@florabase_voc_id, "Priority Four");

insert into term_mapping (source_term_id, target_term_id) values
(90001,2),
(90002,1003),
(90003,1007);

-- ============================== --
-- South Australian Biodiversity - Threatened Fauna
-- ============================== --

set @sab_id=1033;
set @sab_voc_id=100000;

INSERT INTO vocabulary (id, uri, name, description, infosource_id, predicate_id) values 
(@sab_voc_id, "http://ala.org.au/voc/sab", 
"South Australian Biodiversity conservation status", 
"Conservation status taken from South Australian Biodiversity website", 
@sab_id, @hasConservationStatus);

insert into term (id,vocabulary_id,term_string) VALUES 
(100001,@sab_voc_id,"Australia : Not listed. Northern Territory: Vulnerable."),
(100002,@sab_voc_id,"Australia:  Critically Endangered. Northern Territory: Data Deficient."),
(100003,@sab_voc_id,"Australia:  Critically Endangered. Northern Territory: Vulnerable."),
(100004,@sab_voc_id,"Australia:  Endangered (as D. hillieri). Northern Territory: Vulnerable."),
(100005,@sab_voc_id,"Australia:  Endangered.  Northern Territory: Vulnerable."),
(100006,@sab_voc_id,"Australia:  Endangered. Northern Territory: Critically Endangered."),
(100007,@sab_voc_id,"Australia:  Endangered. Northern Territory: Data Deficient."),
(100008,@sab_voc_id,"Australia:  Endangered. Northern Territory: Endangered"),
(100009,@sab_voc_id,"Australia:  Endangered. Northern Territory: Endangered."),
(100010,@sab_voc_id,"Australia:  Endangered. Northern Territory: Extinct."),
(100011,@sab_voc_id,"Australia:  Endangered. Northern Territory: Vulnerable."),
(100012,@sab_voc_id,"Australia:  Endangered."),
(100013,@sab_voc_id,"Australia:  Extinct. Northern Territory: Extinct."),
(100014,@sab_voc_id,"Australia:  Not listed. Northern Territory: Endangered."),
(100015,@sab_voc_id,"Australia:  Not listed. Northern Territory: Extinct."),
(100016,@sab_voc_id,"Australia:  Not listed. Northern Territory: Vulnerable."),
(100017,@sab_voc_id,"Australia:  The subspecies Bettongia lesueur graii is Extinct. Northern Territory: Extinct."),
(100018,@sab_voc_id,"Australia:  Vulnerable (as D. cristicauda). Northern Territory: Vulnerable."),
(100019,@sab_voc_id,"Australia:  Vulnerable. Northern Territory:  Data Deficient."),
(100020,@sab_voc_id,"Australia:  Vulnerable. Northern Territory: Data Deficient."),
(100021,@sab_voc_id,"Australia:  Vulnerable. Northern Territory: Endangered."),
(100022,@sab_voc_id,"Australia:  Vulnerable. Northern Territory: Extinct."),
(100023,@sab_voc_id,"Australia:  Vulnerable. Northern Territory: Least Concern."),
(100024,@sab_voc_id,"Australia:  Vulnerable. Northern Territory: Vulnerable"),
(100025,@sab_voc_id,"Australia:  Vulnerable. Northern Territory: Vulnerable."),
(100026,@sab_voc_id,"Australia: Critically Endangered (east coast population); Vulnerable (west coast population). Northern Territory: Data Deficient."),
(100027,@sab_voc_id,"Australia: Critically Endangered. Northern Territory: Critically Endangered."),
(100028,@sab_voc_id,"Australia: Endangered  Northern Territory: Endangered."),
(100029,@sab_voc_id,"Australia: Endangered (as Ptychosperma bleeseri). Northern Territory: Endangered."),
(100030,@sab_voc_id,"Australia: Endangered Northern Territory:  Least Concern."),
(100031,@sab_voc_id,"Australia: Endangered.  Northern Territory: Endangered."),
(100032,@sab_voc_id,"Australia: Endangered. Northern Territory: Critically Endangered."),
(100033,@sab_voc_id,"Australia: Endangered. Northern Territory: Data Deficient."),
(100034,@sab_voc_id,"Australia: Endangered. Northern Territory: Endangered."),
(100035,@sab_voc_id,"Australia: Endangered. Northern Territory: Extinct in the Wild."),
(100036,@sab_voc_id,"Australia: Extinct. Northern Territory: Data Deficient."),
(100037,@sab_voc_id,"Australia: Not listed.  Northern Territory: Vulnerable."),
(100038,@sab_voc_id,"Australia: Not listed. Northern Territory: Critically Endangered."),
(100039,@sab_voc_id,"Australia: Not Listed. Northern Territory: Endangered"),
(100040,@sab_voc_id,"Australia: Not listed. Northern Territory: Endangered."),
(100041,@sab_voc_id,"Australia: Not listed. Northern Territory: Vulnerable"),
(100042,@sab_voc_id,"Australia: Not listed. Northern Territory: Vulnerable."),
(100043,@sab_voc_id,"Australia: Not listed."),
(100044,@sab_voc_id,"Australia: Vulnerable.  Northern Territory: Near Threatened."),
(100045,@sab_voc_id,"Australia: Vulnerable. Northern Territory: Critically Endangered."),
(100046,@sab_voc_id,"Australia: Vulnerable. Northern Territory: Data Deficient."),
(100047,@sab_voc_id,"Australia: Vulnerable. Northern Territory: Endangered."),
(100048,@sab_voc_id,"Australia: Vulnerable. Northern Territory: Extinct."),
(100049,@sab_voc_id,"Australia: Vulnerable. Northern Territory: Near Threatened."),
(100050,@sab_voc_id,"Australia: Vulnerable. Northern Territory: Vulnerable.");


-- ============================== --
-- PlantNET
-- ============================== --



-- ============================== --
-- Australian Common Insect Names
-- ============================== --

set @aicn_id=1003;
set @aicn_voc_id=11000;

INSERT INTO vocabulary (id, uri, name, description, infosource_id, predicate_id) values 
(@aicn_voc_id, "http://ala.org.au/voc/aicn", "Australian Insect Common Names Pest Status", "Pest Status values used on Australian Insect Common Names species pages", @aicn_id, @hasPestStatus);

-- insert padil pest terms
INSERT INTO term (id,vocabulary_id,term_string) VALUES (11001,@aicn_voc_id, "Native");
INSERT INTO term (id,vocabulary_id,term_string) VALUES (11002,@aicn_voc_id, "Exotic");
INSERT INTO term (id,vocabulary_id,term_string) VALUES (11003,@aicn_voc_id, "Biological Control Agent");

insert into term_mapping (source_term_id, target_term_id) values
(11001,4),
(11002,2),
(11003,1);

-- ============================== --
-- Wikipedia
-- ============================== --


-- ============================== --
-- Australian Native Plants Society
-- ============================== --

