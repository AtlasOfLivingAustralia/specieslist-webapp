/**************************************************************************
 *  Copyright (C) 2010 Atlas of Living Australia
 *  All Rights Reserved.
 *
 *  The contents of this file are subject to the Mozilla Public
 *  License Version 1.1 (the "License"); you may not use this file
 *  except in compliance with the License. You may obtain a copy of
 *  the License at http://www.mozilla.org/MPL/
 *
 *  Software distributed under the License is distributed on an "AS
 *  IS" basis, WITHOUT WARRANTY OF ANY KIND, either express or
 *  implied. See the License for the specific language governing
 *  rights and limitations under the License.
 ***************************************************************************/

package org.ala.util;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

/**
 * Enum class to store and retrieve status types
 *
 * @author "Nick dos Remedios <Nick.dosRemedios@csiro.au>"
 */
public enum StatusType {
    CONSERVATION("conservationStatus", "Conservation Status"),
    PEST("pestStatus", "Pest Status");

    private String value;
    private String displayName;

    private StatusType(String value, String name) {
        this.value = value;
        this.displayName = name;
    }

    /**
     * Allow reverse-lookup
     * (based on <a href="http://www.ajaxonomy.com/2007/java/making-the-most-of-java-50-enum-tricks">Enum Tricks</a>)
     */
    private static final Map<String,StatusType> statusTypeLookup = new HashMap<String,StatusType>();

    static {
         for (StatusType st : EnumSet.allOf(StatusType.class)) {
             statusTypeLookup.put(st.getValue(), st);
         }
    }

    /**
     * Lookup method for status type string
     *
     * @param statusType
     * @return StatusType the StatusType
     */
    public static StatusType getForStatusType(String statusType) {
        return statusTypeLookup.get(statusType);
    }

    @Override
    public String toString() {
        return this.value;
    }

    public String getValue() {
        return value;
    }

    public String getDisplayName() {
        return displayName;
    }
}