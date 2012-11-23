package org.ala.dao;

public enum Regions {
	AUS("Australia", "country", "AUS"),
	VIC("Victoria", "state", "VIC"),
	NSW("New South Wales", "state", "NSW"),
	ACT("Australian Capital Territory", "state", "ACT"),
	QLD("Queensland", "state", "QLD"),
	NT("Northern Territory", "state", "NT"),
	TAS("Tasmania", "state", "TAS"),
	WA("Western Australia", "state", "WA"),
	SA("South Australia",  "state", "SA");
	
	private String name,type,acronym;

	private Regions(String name, String type, String acronym) {
		this.name = name;
		this.type = type;
		this.acronym = acronym;
	}
	
    public static Regions getRegion(String name){
    	if(name==null) return null;
    	for(Regions r :values()){
    		if(name.equalsIgnoreCase(r.name)){
    			return r;
    		}
    	}
    	return null;
    }

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @return the type
	 */
	public String getType() {
		return type;
	}

	/**
	 * @param type the type to set
	 */
	public void setType(String type) {
		this.type = type;
	}

	/**
	 * @return the acronym
	 */
	public String getAcronym() {
		return acronym;
	}

	/**
	 * @param acronym the acronym to set
	 */
	public void setAcronym(String acronym) {
		this.acronym = acronym;
	}
}
