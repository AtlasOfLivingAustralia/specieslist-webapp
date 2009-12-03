/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package csiro.diasb.datamodels;

import java.util.ArrayList;

/**
 * Facet results as returned from a SOLR search with facets enabled
 * @author oak021
 */
public class FacetResult {
    /**
     * Name of the field being treated as a facet
     */
    private String fieldName;
    /**
     * Set of facet field results
     */
    private ArrayList<FieldResult> fieldResult;


    /**
     * COnstructor
     * @param fieldName Field used as a facet
     * @param fieldResult Terms and counts returned from a facet search on this field
     */
    public FacetResult(String fieldName, ArrayList<FieldResult> fieldResult) {
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
    public ArrayList<FieldResult> getFieldResult() {
        return fieldResult;
    }

    /**
     *
     * @param fieldResult
     */
    public void setFieldResult(ArrayList<FieldResult> fieldResult) {
        this.fieldResult = fieldResult;
    }

}
