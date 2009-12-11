/**
 * Copyright (c) CSIRO Australia, 2009
 *
 * @author $Author: oak021 $
 * @version $Id:  FacetQuery.java 697 2009-08-01 00:23:45Z oak021 $
 */

package csiro.diasb.fedora;

import org.apache.solr.client.solrj.SolrQuery;

/**
 * Extends the SolrQuery object with dynamic facet constraint and page management
 * @author oak021
 */
public class FacetQuery extends SolrQuery {

    /**
     * Constructs a facet query with the default facet, sort and highlight fields already set
     * The default facet fields are: ContentModel, Rank, Publication Type, CollectionID and GeographicID
     * The default highlight fields are: text_all, dc.title, dc.description, dc.hasRank, dc.hasRegion,
     * dc.hasCountry and dc.hasContentModel
     */
    public FacetQuery() {
        setQueryType("standard");
        setFacet(true);
        //addFacetField("ContentModel");
        addFacetField("Rank");
        addFacetField("PublicationType");
        addFacetField("CollectionID");
        addFacetField("GeographicRegionID");
        setFacetMinCount(1);
        addSortField("dc2.title", SolrQuery.ORDER.asc);
        setRows(10);
        setStart(0);

        //add highlights
        setHighlight(true);
        addHighlightField("text_all");
        addHighlightField("dc.title");
        addHighlightField("dc.description");
        addHighlightField("rdf.hasRank");
        addHighlightField("rdf.hasRegion");
        addHighlightField("rdf.hasCountry");
        addHighlightField("rdf.hasContentModel");
        //Note that this wildcard highlight field doesn't seem to work, although the SOLR documentation
        //suggests that it should
        addHighlightField("rdf.has*");
    }
   /**
     * Directs the standard facet query to constrain search results to a particular choice
     * of field value, for example ContentModel must be ala:ImageContentModel
     * Used in response to a faceted search
     * @param fieldName Name of field to be contrained
     * @param constraint Value to constrain the field to
     */
    public void addFacetConstraint(String fieldName, String constraint)
    {
        boolean b = removeFacetField(escapeComponent(fieldName));
        if (b) addFilterQuery(escapeComponent(fieldName)+":"+escapeComponent(constraint));
    }
    /**
     * Directs the standard facet query to remove a facet constraint from the standard facet query.
     * @param filterQuery Existing facet constraint to be removed
     */
    public void removeFacetConstraint(String filterQuery)
    {
        //see if the constraint value is available
        if ( filterQuery.indexOf(':')<0)
        {
            //need to find out the value of the current constraint
        }
        removeFilterQuery(filterQuery);
        String fieldName = filterQuery.substring(0, filterQuery.indexOf(':'));
        addFacetField(fieldName);
    }
    /**
     *Directs the standard facet query to remove all facet constraints from the standard facet query.
     * (Facet constraints are set using addFacetConstraint)
     */
    public void removeAllFacetConstraints()
    {
        String s[] = getFilterQueries();
        //I probably need to parse this first
        for (String str : s)
        {
            removeFacetConstraint(str);
        }
    }
/**
 * Adds a backslash before any of the following characters: + - & || ! ( ) { } [ ] ^ " ~  : \
 *
 * @param s Input String
 * @return The escaped String
 */
    public static String  escapeComponent(String s)
    {
        //list of chars that need to be escaped:  + - & || ! ( ) { } [ ] ^ " ~ * ? : \
        String e = s.replace("+","\\+").replace("-","\\-").replace("&","\\&").replace("||","\\||");
        String f = e.replace("!","\\!").replace("(","\\(").replace(")","\\)").replace("{","\\{").replace("}","\\}");
        e =  f.replace("[","\\[").replace("]","\\]").replace("^","\\^").replace("\"","\\\"");
      //  f =  e.replace("~","\\~").replace("*","\\*").replace("?","\\?").replace(":","\\:").replace("\\","\\\\");
        //I've removed * and ? from here because this escaping is done after I've added wildcards
        f =  e.replace("~","\\~").replace(":","\\:").replace("\\","\\\\");
       return f;
    }

    /**
     * Changes the start row of facetQuery to the next page (incrementing it by
     * the current number of result rows returned by the query)
     */
    public void setToNextPage() {
        setStart(getStart()+getRows());
    }
    /**
     * Changes the start row of facetQuery to the previous page (decrementing it by
     * the current number of result rows returned by the query)
     */
    public void setToPrevPage() {
        setStart(getStart()- getRows());
    }
}
