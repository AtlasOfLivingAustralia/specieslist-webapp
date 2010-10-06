package org.ala.model;

public class SpecimenHolding extends AttributableObject implements Comparable<SpecimenHolding> {
	private String url;
	private String institutionName;
	private String siteName;
	private String family;
	private String genus;
	private String hybirdQualifier;
	private String species;
	private String scientificName;
	private String infraspecificQualifier;
	private String infraspecificName;
	private String cultivar;
	private String commonName;
	private String notes;
	private int count;
	
	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getInstitutionName() {
		return institutionName;
	}

	public void setInstitutionName(String institutionName) {
		this.institutionName = institutionName;
	}

	public String getSiteName() {
		return siteName;
	}

	public void setSiteName(String siteName) {
		this.siteName = siteName;
	}

	public String getFamily() {
		return family;
	}

	public void setFamily(String family) {
		this.family = family;
	}

	public String getGenus() {
		return genus;
	}

	public void setGenus(String genus) {
		this.genus = genus;
	}

	public String getHybirdQualifier() {
		return hybirdQualifier;
	}

	public void setHybirdQualifier(String hybirdQualifier) {
		this.hybirdQualifier = hybirdQualifier;
	}

	public String getSpecies() {
		return species;
	}

	public void setSpecies(String species) {
		this.species = species;
	}

	public String getScientificName() {
		return scientificName;
	}

	public void setScientificName(String scientificName) {
		this.scientificName = scientificName;
	}

	public String getInfraspecificQualifier() {
		return infraspecificQualifier;
	}

	public void setInfraspecificQualifier(String infraspecificQualifier) {
		this.infraspecificQualifier = infraspecificQualifier;
	}

	public String getInfraspecificName() {
		return infraspecificName;
	}

	public void setInfraspecificName(String infraspecificName) {
		this.infraspecificName = infraspecificName;
	}

	public String getCultivar() {
		return cultivar;
	}

	public void setCultivar(String cultivar) {
		this.cultivar = cultivar;
	}

	public String getCommonName() {
		return commonName;
	}

	public void setCommonName(String commonName) {
		this.commonName = commonName;
	}

	public String getNotes() {
		return notes;
	}

	public void setNotes(String notes) {
		this.notes = notes;
	}

	public int getCount() {
		return count;
	}

	public void setCount(int count) {
		this.count = count;
	}
	
	@Override
	public int compareTo(SpecimenHolding o) {
		if (o.getScientificName() != null && this.getScientificName() != null) {
			return this.getScientificName().compareTo(o.getScientificName());
		}
		return -1;
	}
	
	/**
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if(obj!=null && obj instanceof SpecimenHolding){
			SpecimenHolding sh = (SpecimenHolding) obj;
			if(sh.getUrl()!=null && this.url!=null && this.scientificName != null && sh.getScientificName() != null){
				return (sh.getUrl().equals(this.url) && sh.getScientificName().equals(this.scientificName));
			}
		}
		return false;
	}	
}
