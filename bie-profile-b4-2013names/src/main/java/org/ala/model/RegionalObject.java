package org.ala.model;

/**
 * A model object that provides some regional information for a 
 * profile data. ie when profile information is only applicable in 
 * certain regions.
 *
 * @author Natasha Carter
 */
public abstract class RegionalObject extends AttributableObject {

  private String stateProvince; 
  private String locality;  
  private String footprintWKT;  
  private String locationID;
  /**
   * @return the stateProvince
   */
  public String getStateProvince() {
    return stateProvince;
  }
  /**
   * @param stateProvince the stateProvince to set
   */
  public void setStateProvince(String stateProvince) {
    this.stateProvince = stateProvince;
  }
  /**
   * @return the locality
   */
  public String getLocality() {
    return locality;
  }
  /**
   * @param locality the locality to set
   */
  public void setLocality(String locality) {
    this.locality = locality;
  }
  /**
   * @return the footprintWKT
   */
  public String getFootprintWKT() {
    return footprintWKT;
  }
  /**
   * @param footprintWKT the footprintWKT to set
   */
  public void setFootprintWKT(String footprintWKT) {
    this.footprintWKT = footprintWKT;
  }
  /**
   * @return the locationID
   */
  public String getLocationID() {
    return locationID;
  }
  /**
   * @param locationID the locationID to set
   */
  public void setLocationID(String locationID) {
    this.locationID = locationID;
  }
  
}
