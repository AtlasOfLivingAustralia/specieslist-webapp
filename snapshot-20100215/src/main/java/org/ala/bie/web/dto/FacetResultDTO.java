/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.ala.bie.web.dto;

import java.util.ArrayList;

/**
 * Facet results as returned from a SOLR search with facets enabled
 * 
 * @author oak021
 */
public class FacetResultDTO {
    /** Name of the field being treated as a facet */
    private String fieldName;
    /** Set of facet field results */
    private ArrayList<FieldResultDTO> fieldResult;


    /**
     * COnstructor
     * @param fieldName Field used as a facet
     * @param fieldResult Terms and counts returned from a facet search on this field
     */
    public FacetResultDTO(String fieldName, ArrayList<FieldResultDTO> fieldResult) {
        this.fieldName = fieldName;
        this.fieldResult = fieldResult;
    }

    /**
     *
     * @return
     */
    public String getFieldName() {
        return fieldName;
    }

    /**
     *
     * @param fieldName
     */
    public void setFieldName(String fieldName) {
        this.fieldName = fieldName;
    }

    /**
     *
     * @return
     */
    public ArrayList<FieldResultDTO> getFieldResult() {
        return fieldResult;
    }

    /**
     *
     * @param fieldResult
     */
    public void setFieldResult(ArrayList<FieldResultDTO> fieldResult) {
        this.fieldResult = fieldResult;
    }

}
