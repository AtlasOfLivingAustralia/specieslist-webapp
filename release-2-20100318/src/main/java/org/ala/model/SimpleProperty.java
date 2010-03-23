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
package org.ala.model;

import org.ala.repository.Predicates;
import org.apache.log4j.Logger;

/**
 * Bean representing a simple harvested property (key value pair), where the property name is
 * from a controlled vocabulary (see {@see org.ala.repository.Predicates})
 *
 * @author "Nick dos Remedios <Nick.dosRemedios@csiro.au>"
 */
public class SimpleProperty extends AttributableObject implements Comparable<SimpleProperty> {
    /** logger */
    private final static Logger logger = Logger.getLogger(SimpleProperty.class);
	/** The property name - using a controlled vocabulary ({@see org.ala.repository.Predicates}) */
	protected String name;
	/** The property value supplying the name e.g. IUCN */
	protected String value;
    /** The title of the resource contributing the property */
    protected String title;
    /** The identifier (URI) of the resource contributing the property */
    protected String identifier;

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final SimpleProperty other = (SimpleProperty) obj;
        if ((this.name == null) ? (other.name != null) : !this.name.equals(other.name)) {
            return false;
        }
        if ((this.value == null) ? (other.value != null) : !this.value.equals(other.value)) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 97 * hash + (this.name != null ? this.name.hashCode() : 0);
        hash = 97 * hash + (this.value != null ? this.value.hashCode() : 0);
        return hash;
    }
	
	@Override
	public int compareTo(SimpleProperty o) {
		/* convert name to a Predicates Enum type and use the category field to sort */
        try {
            Integer thisCat = Predicates.getForPredicate(this.name).getCategory();
            Integer otherCat = Predicates.getForPredicate(o.getName()).getCategory();
            return thisCat.compareTo(otherCat);
        } catch (Exception e) {
            logger.error("Could not find the Predicates enum constant for either: "+name+" or "+o.getName(), e);
        }
		return -1;
	}

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    /**
     * Get the value of title
     *
     * @return the value of title
     */
    public String getTitle() {
        return title;
    }

    /**
     * Set the value of title
     *
     * @param title new value of title
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * Get the value of identifier
     *
     * @return the value of identifier
     */
    public String getIdentifier() {
        return identifier;
    }

    /**
     * Set the value of identifier
     *
     * @param identifier new value of identifier
     */
    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

}
