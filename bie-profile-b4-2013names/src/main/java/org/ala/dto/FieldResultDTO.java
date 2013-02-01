/***************************************************************************
 * Copyright (C) 2010 Atlas of Living Australia
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
package org.ala.dto;

/**
 *
 * @author oak021
 */
public class FieldResultDTO {
    String label;
    String prefix;
    long count;

    public FieldResultDTO(String fieldValue, long count) {
        setFieldValue(fieldValue) ;
        this.count = count;
    }
    public void setFieldValue(String fieldValue)
    {
        //Currently not using the prefix, currently
        //the field values are expected to shortened during
        //the fedora indexing process. Full-stops are replaced
        //here by uderscores as the jsp processor interprets full-stops
        //as special characters and doesn't forward to the correct action
        this.label = fieldValue.replace('.', '_');
    }
    public String getFieldValue()
    {
        if(prefix != null)
            return prefix+label;
        else
            return label;
    }
    public long getCount() {
        return count;
    }

    public void setCount(long count) {
        this.count = count;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getPrefix() {
        return prefix;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

}
