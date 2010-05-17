package org.ala.model;

public class Classification extends AttributableObject implements Comparable<Classification>{

	protected String guid;
	protected String scientificName;
	protected String rank;
    protected Integer rankId;
	protected String kingdom;
	protected String kingdomGuid;
	protected String phylum;
	protected String phylumGuid;
	protected String clazz;
	protected String clazzGuid;
	protected String order;
	protected String orderGuid;
	protected String superfamily;
	protected String superfamilyGuid;
	protected String family;
	protected String familyGuid;
	protected String genus;
	protected String genusGuid;
	protected String species;
	protected String speciesGuid;
	protected String subspecies;
	protected String subspeciesGuid;
	
	/**
	 * @return the kingdom
	 */
	public String getKingdom() {
		return kingdom;
	}
	/**
	 * @param kingdom the kingdom to set
	 */
	public void setKingdom(String kingdom) {
		this.kingdom = kingdom;
	}
	/**
	 * @return the kingdomGuid
	 */
	public String getKingdomGuid() {
		return kingdomGuid;
	}
	/**
	 * @param kingdomGuid the kingdomGuid to set
	 */
	public void setKingdomGuid(String kingdomGuid) {
		this.kingdomGuid = kingdomGuid;
	}
	/**
	 * @return the phylum
	 */
	public String getPhylum() {
		return phylum;
	}
	/**
	 * @param phylum the phylum to set
	 */
	public void setPhylum(String phylum) {
		this.phylum = phylum;
	}
	/**
	 * @return the phylumGuid
	 */
	public String getPhylumGuid() {
		return phylumGuid;
	}
	/**
	 * @param phylumGuid the phylumGuid to set
	 */
	public void setPhylumGuid(String phylumGuid) {
		this.phylumGuid = phylumGuid;
	}
	/**
	 * @return the clazz
	 */
	public String getClazz() {
		return clazz;
	}
	/**
	 * @param clazz the clazz to set
	 */
	public void setClazz(String clazz) {
		this.clazz = clazz;
	}
	/**
	 * @return the clazzGuid
	 */
	public String getClazzGuid() {
		return clazzGuid;
	}
	/**
	 * @param clazzGuid the clazzGuid to set
	 */
	public void setClazzGuid(String clazzGuid) {
		this.clazzGuid = clazzGuid;
	}
	/**
	 * @return the order
	 */
	public String getOrder() {
		return order;
	}
	/**
	 * @param order the order to set
	 */
	public void setOrder(String order) {
		this.order = order;
	}
	/**
	 * @return the orderGuid
	 */
	public String getOrderGuid() {
		return orderGuid;
	}
	/**
	 * @param orderGuid the orderGuid to set
	 */
	public void setOrderGuid(String orderGuid) {
		this.orderGuid = orderGuid;
	}
	/**
	 * @return the superfamily
	 */
	public String getSuperfamily() {
		return superfamily;
	}
	/**
	 * @param superfamily the superfamily to set
	 */
	public void setSuperfamily(String superfamily) {
		this.superfamily = superfamily;
	}
	/**
	 * @return the superfamilyGuid
	 */
	public String getSuperfamilyGuid() {
		return superfamilyGuid;
	}
	/**
	 * @param superfamilyGuid the superfamilyGuid to set
	 */
	public void setSuperfamilyGuid(String superfamilyGuid) {
		this.superfamilyGuid = superfamilyGuid;
	}
	/**
	 * @return the family
	 */
	public String getFamily() {
		return family;
	}
	/**
	 * @param family the family to set
	 */
	public void setFamily(String family) {
		this.family = family;
	}
	/**
	 * @return the familyGuid
	 */
	public String getFamilyGuid() {
		return familyGuid;
	}
	/**
	 * @param familyGuid the familyGuid to set
	 */
	public void setFamilyGuid(String familyGuid) {
		this.familyGuid = familyGuid;
	}
	/**
	 * @return the genus
	 */
	public String getGenus() {
		return genus;
	}
	/**
	 * @param genus the genus to set
	 */
	public void setGenus(String genus) {
		this.genus = genus;
	}
	/**
	 * @return the genusGuid
	 */
	public String getGenusGuid() {
		return genusGuid;
	}
	/**
	 * @param genusGuid the genusGuid to set
	 */
	public void setGenusGuid(String genusGuid) {
		this.genusGuid = genusGuid;
	}
	/**
	 * @return the species
	 */
	public String getSpecies() {
		return species;
	}
	/**
	 * @param species the species to set
	 */
	public void setSpecies(String species) {
		this.species = species;
	}
	/**
	 * @return the speciesGuid
	 */
	public String getSpeciesGuid() {
		return speciesGuid;
	}
	/**
	 * @param speciesGuid the speciesGuid to set
	 */
	public void setSpeciesGuid(String speciesGuid) {
		this.speciesGuid = speciesGuid;
	}
	/**
	 * @return the subspecies
	 */
	public String getSubspecies() {
		return subspecies;
	}
	/**
	 * @param subspecies the subspecies to set
	 */
	public void setSubspecies(String subspecies) {
		this.subspecies = subspecies;
	}
	/**
	 * @return the subspeciesGuid
	 */
	public String getSubspeciesGuid() {
		return subspeciesGuid;
	}
	/**
	 * @param subspeciesGuid the subspeciesGuid to set
	 */
	public void setSubspeciesGuid(String subspeciesGuid) {
		this.subspeciesGuid = subspeciesGuid;
	}
	/**
	 * @return the guid
	 */
	public String getGuid() {
		return guid;
	}
	/**
	 * @param guid the guid to set
	 */
	public void setGuid(String guid) {
		this.guid = guid;
	}
	/**
	 * @return the scientificName
	 */
	public String getScientificName() {
		return scientificName;
	}
	/**
	 * @param scientificName the scientificName to set
	 */
	public void setScientificName(String scientificName) {
		this.scientificName = scientificName;
	}
	
	@Override
	public int compareTo(Classification o) {
		return 0;
	}
	/**
	 * @see org.ala.model.AttributableObject#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if(obj!=null && obj instanceof Classification){
			
			Classification classification = (Classification) obj;
			if(classification.getInfoSourceId()==null && this.getInfoSourceId()==null){
				return true;
			}
			if(classification.getInfoSourceId()==null && this.getInfoSourceId()!=null){
				return false;
			}
			if(classification.getInfoSourceId()!=null && this.getInfoSourceId()==null){
				return false;
			}
			return classification.getInfoSourceId().equals(this.getInfoSourceId());
		}
		
		return false;
	}
	/**
	 * @return the rank
	 */
	public String getRank() {
		return rank;
	}
	/**
	 * @param rank the rank to set
	 */
	public void setRank(String rank) {
		this.rank = rank;
	}
    /**
     * @return the rankId
     */
    public Integer getRankId() {
        return rankId;
    }
	/**
	 * @param rankId the rankId to set
	 */
    public void setRankId(Integer rankId) {
        this.rankId = rankId;
    }
    
	/**
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("Classification [clazz=");
		builder.append(this.clazz);
		builder.append(", clazzGuid=");
		builder.append(this.clazzGuid);
		builder.append(", family=");
		builder.append(this.family);
		builder.append(", familyGuid=");
		builder.append(this.familyGuid);
		builder.append(", genus=");
		builder.append(this.genus);
		builder.append(", genusGuid=");
		builder.append(this.genusGuid);
		builder.append(", guid=");
		builder.append(this.guid);
		builder.append(", kingdom=");
		builder.append(this.kingdom);
		builder.append(", kingdomGuid=");
		builder.append(this.kingdomGuid);
		builder.append(", order=");
		builder.append(this.order);
		builder.append(", orderGuid=");
		builder.append(this.orderGuid);
		builder.append(", phylum=");
		builder.append(this.phylum);
		builder.append(", phylumGuid=");
		builder.append(this.phylumGuid);
		builder.append(", rank=");
		builder.append(this.rank);
		builder.append(", rankId=");
		builder.append(this.rankId);
		builder.append(", scientificName=");
		builder.append(this.scientificName);
		builder.append(", species=");
		builder.append(this.species);
		builder.append(", speciesGuid=");
		builder.append(this.speciesGuid);
		builder.append(", subspecies=");
		builder.append(this.subspecies);
		builder.append(", subspeciesGuid=");
		builder.append(this.subspeciesGuid);
		builder.append(", superfamily=");
		builder.append(this.superfamily);
		builder.append(", superfamilyGuid=");
		builder.append(this.superfamilyGuid);
		builder.append(", documentId=");
		builder.append(this.documentId);
		builder.append(", infoSourceId=");
		builder.append(this.infoSourceId);
		builder.append(", infoSourceName=");
		builder.append(this.infoSourceName);
		builder.append(", infoSourceURL=");
		builder.append(this.infoSourceURL);
		builder.append("]");
		return builder.toString();
	}
}
