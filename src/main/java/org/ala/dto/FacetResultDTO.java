/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.ala.dto;

import java.util.ArrayList;

/**
 * Facet result for a SOLR search
 * 
 * @author "Nick dos Remedios <Nick.dosRemedios@csiro.au>"
 */
public class FacetResultDTO {
    /** Name of the field being treated as a facet */
    private String fieldName;
    /** Set of facet field results */
    private ArrayList<FieldResultDTO> fieldResult;


    /**
     * Constructor
     * 
     * @param fieldName Field used as a facet
     * @param fieldResult Terms and counts returned from a facet search on this field
     */
    public FacetResultDTO(String fieldName, ArrayList<FieldResultDTO> fieldResult) {
        this.fieldName = fieldName;
        this.fieldResult = fieldResult;
    }

    /**
     * Default constructor
     */
    public FacetResultDTO() {}

    /*
     * Getters & Setters
     */
    public String getFieldName() {
        return fieldName;
    }

    public void setFieldName(String fieldName) {
        this.fieldName = fieldName;
    }

    public ArrayList<FieldResultDTO> getFieldResult() {
        return fieldResult;
    }

    public void setFieldResult(ArrayList<FieldResultDTO> fieldResult) {
        this.fieldResult = fieldResult;
    }

}
