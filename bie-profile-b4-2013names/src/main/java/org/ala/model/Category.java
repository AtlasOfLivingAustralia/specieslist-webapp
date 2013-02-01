package org.ala.model;

public class Category extends RegionalObject implements Comparable<Category> {

  private String startDate; 
  private String endDate;
  private String category;  
  private String authority; 
  //private String references; //the identifier  
  private String reason;  
  private String categoryRemarks;
  
  @Override
  public int compareTo(Category cat) {
      if(cat.getCategory()!=null && category!=null){
          return category.compareTo(cat.getCategory());
      }
      return -1;
    
  }
  
  @Override
  public boolean equals(Object obj){
      if(obj instanceof Category){
          Category cat = (Category)obj;
          return cat.getInfoSourceUid().equals(infoSourceUid) && category != null && category.equals(cat.getCategory());
      }
      return false;
  }

  /**
   * @return the startDate
   */
  public String getStartDate() {
    return startDate;
  }

  /**
   * @param startDate the startDate to set
   */
  public void setStartDate(String startDate) {
    this.startDate = startDate;
  }

  /**
   * @return the endDate
   */
  public String getEndDate() {
    return endDate;
  }

  /**
   * @param endDate the endDate to set
   */
  public void setEndDate(String endDate) {
    this.endDate = endDate;
  }

  /**
   * @return the category
   */
  public String getCategory() {
    return category;
  }

  /**
   * @param category the category to set
   */
  public void setCategory(String category) {
    this.category = category;
  }

  /**
   * @return the authority
   */
  public String getAuthority() {
    return authority;
  }

  /**
   * @param authority the authority to set
   */
  public void setAuthority(String authority) {
    this.authority = authority;
  }

  /**
   * @return the reason
   */
  public String getReason() {
    return reason;
  }

  /**
   * @param reason the reason to set
   */
  public void setReason(String reason) {
    this.reason = reason;
  }

  /**
   * @return the categoryRemarks
   */
  public String getCategoryRemarks() {
    return categoryRemarks;
  }

  /**
   * @param categoryRemarks the categoryRemarks to set
   */
  public void setCategoryRemarks(String categoryRemarks) {
    this.categoryRemarks = categoryRemarks;
  }
  
  

}
