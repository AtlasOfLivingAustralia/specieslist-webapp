
package org.ala.model;

/**
 *
 * A POJO for a synonym - an extension of a Taxon Concept to include synonym type information
 *
 * @author Natasha Carter
 */
public class SynonymConcept extends TaxonConcept{
    protected Integer type;
    protected String relationship;
    protected String description;

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getRelationship() {
        return relationship;
    }

    public void setRelationship(String relationship) {
        this.relationship = relationship;
    }

    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
    }
    @Override
    public String toString(){
        StringBuilder builder = new StringBuilder();
        builder.append("SynonymConcept[type ").append(type.toString());
        builder.append(", relationship ").append(relationship);
        builder.append(", description").append(description);
        builder.append(super.toString());
        builder.append("]");
        return builder.toString();
    }
    
    /**
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     * 
     * Want synonyms order by name but having the preferred concept first when multiple names.
     * 
     */
    @Override
    public int compareTo(TaxonConcept o) {
        //check the infosources
        if(o.getNameString()!=null && nameString!=null){
            if(o.getNameString().equals(nameString)){
                if(o.isPreferred != isPreferred){
                    if(isPreferred)
                        return -1;
                    else return 1;
                }                
            }
            return nameString.compareTo(o.getNameString());
        }
        return -1;
    }   


}
