/* **************************************************************************
 * Copyright (C) 2009 Atlas of Living Australia
 * All Rights Reserved.
 *
 * The contents of this file are subject to the Mozilla Public
 * License Version 1.1 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of
 * the License at http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS
 * IS" basis, WITHOUT WARRANTY OF ANY KIND, either express or
 * implied. See the License for the specific language governing
 * rights and limitations under the License.
 ***************************************************************************/

package org.ala.bie.web.util;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

/**
 * Enum to store values for ContentModel used in Fedora Commons
 *
 * @author "Nick dos Remedios <Nick.dosRemedios@csiro.au>"
 */
public enum ContentModelEnum {
    TAXON_CONCEPT ("ala:TaxonConceptContentModel","species","TC"),
    TAXON_NAME    ("ala:TaxonNameContentModel","name","TN"),
    PUBLICATION   ("ala:PublicationContentModel","pub","PUB"),
    IMAGE         ("ala:ImageContentModel","image","IMG"),
    HTML_PAGE     ("ala:HtmlPageContentModel","html","HTML"),
    DATA_STREAM   ("","datastream","-"); // null needs to be changes to something?

    private String id;
    private String name;
    private String code;

    /*
     * Allow reverse-lookup
     * (based on http://www.ajaxonomy.com/2007/java/making-the-most-of-java-50-enum-tricks)
     */
    private static final Map<String,ContentModelEnum> idLookup
          = new HashMap<String,ContentModelEnum>();
    private static final Map<String,ContentModelEnum> nameLookup
          = new HashMap<String,ContentModelEnum>();

    static {
         for (ContentModelEnum cm : EnumSet.allOf(ContentModelEnum.class)) {
             nameLookup.put(cm.getName(), cm);
             idLookup.put(cm.getId(), cm);
         }
    }

    /**
     * Lookup method for id field
     *
     * @param id
     * @return ContentModelEnum the ContentModelEnum
     */
    public static ContentModelEnum getForId(String id) {
        return idLookup.get(id);
    }
    
    /**
     * Lookup method for id field
     *
     * @param name
     * @return ContentModelEnum the ContentModelEnum
     */
    public static ContentModelEnum getForName(String name) {
        return nameLookup.get(name);
    }

    /**
     * Constructor for setting the 'value'
     * @param field value as String
     */
    private ContentModelEnum(String id, String name, String code) {
        this.id = id;
        this.name = name;
        this.code = code;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
    
}