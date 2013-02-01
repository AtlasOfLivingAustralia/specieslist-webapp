package org.ala.util;


import org.ala.model.Classification;
import org.apache.commons.collections.ListUtils;
import org.apache.commons.lang.StringUtils;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.JavaType;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.util.*;

@Component("speciesGroupsUtil")
public class SpeciesGroupsUtil {

	protected Set<String> fishTaxa = new HashSet<String>();
    protected List<Subgroup> subgroups = new ArrayList<Subgroup>();
    protected Map<String,String> groupMap = new HashMap<String,String>() ;
    protected Map<String,String> scientificNameToCommonName = new HashMap<String,String>();

    public SpeciesGroupsUtil() throws Exception {

        InputStream input = getClass().getResourceAsStream("/subgroups.json");
        ObjectMapper om = new ObjectMapper();
        JavaType type = om.getTypeFactory().constructCollectionType(List.class, Subgroup.class);
        this.subgroups = om.readValue(input, type);
        for(Subgroup group: subgroups){
            for(Map<String,String> taxon: group.taxa) {
              groupMap.put(taxon.get("name").trim().toLowerCase(),taxon.get("common").trim());
              scientificNameToCommonName.put(taxon.get("name").trim().toLowerCase(),taxon.get("common").trim());
            }
        }

	    fishTaxa.add("Myxini".toLowerCase());
		fishTaxa.add("Chondrichthyes".toLowerCase());
		fishTaxa.add("Sarcopterygii".toLowerCase());
		fishTaxa.add("Actinopterygii".toLowerCase());
    }

    public List<String> getSpeciesSubgroup(Classification classification){
        List<String> groups = new ArrayList<String>();
        if(classification.getOrder() != null) addIfNotNull(groupMap.get(classification.getOrder().toLowerCase()), groups);
        if(classification.getFamily() != null) addIfNotNull(groupMap.get(classification.getFamily().toLowerCase()), groups);
        if(classification.getClazz() != null) addIfNotNull(groupMap.get(classification.getClazz().toLowerCase()), groups);
        if(classification.getPhylum() != null) addIfNotNull(groupMap.get(classification.getPhylum().toLowerCase()), groups);
        if(classification.getGenus() != null) addIfNotNull(groupMap.get(classification.getGenus().toLowerCase()), groups);
        return groups;
    }

    private void addIfNotNull(String el, List<String> list){
        if(el != null) list.add(el);
    }


    public List<String> getSpeciesGroup(Classification classification){

        List<String> speciesGroups = new ArrayList<String>();

        //speciesGroup
        if(StringUtils.isNotBlank(classification.getPhylum())){
            if("arthropoda".equals(classification.getPhylum().toLowerCase())) speciesGroups.add( "Arthropods");
            if("mollusca".equals(classification.getPhylum().toLowerCase())) speciesGroups.add("Molluscs");
            if("magnoliophyta".equals(classification.getPhylum().toLowerCase())) speciesGroups.add("Flowering plants");
        }
        if(StringUtils.isNotBlank(classification.getClazz())){
            if("reptilia".equals(classification.getClazz().toLowerCase())) speciesGroups.add("Reptiles");
            if("amphibia".equals(classification.getClazz().toLowerCase())) speciesGroups.add("Frogs");
            if("aves".equals(classification.getClazz().toLowerCase())) speciesGroups.add("Birds");
            if("mammalia".equals(classification.getClazz().toLowerCase())) speciesGroups.add("Mammals");
        }
        if(StringUtils.isNotBlank(classification.getKingdom())){
            if("plantae".equals(classification.getKingdom().toLowerCase())) speciesGroups.add("Plants");
            if("animalia".equals(classification.getKingdom().toLowerCase())) speciesGroups.add("Animals");
        }
        if(classification.getClazz()!=null && fishTaxa.contains(classification.getClazz().toLowerCase())){
            speciesGroups.add("Fish");
        }
        return speciesGroups;
    }

    public static class Subgroup {
        public String speciesGroup;
        public String taxonRank;
        public String facetName;
        public List<Map<String,String>> taxa;

        public String getSpeciesGroup() {
            return speciesGroup;
        }

        public void setSpeciesGroup(String speciesGroup) {
            this.speciesGroup = speciesGroup;
        }

        public String getTaxonRank() {
            return taxonRank;
        }

        public void setTaxonRank(String taxonRank) {
            this.taxonRank = taxonRank;
        }

        public  List<Map<String,String>> getTaxa() {
            return taxa;
        }

        public void setTaxa( List<Map<String,String>> taxa) {
            this.taxa = taxa;
        }
    }
}
