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

/**
 * Enum class to store & retrieve repository file types
 *
 * @author "Nick dos Remedios <Nick.dosRemedios@csiro.au>"
 */
public enum FileType {
    DC  ("dc", 2),
    RDF ("rdf", 3),
    RAW ("raw", null);

    private String filename;
    private Integer fieldCount;

    private FileType(String fn, Integer fc) {
        this.filename = fn;
        this.fieldCount = fc;
    }

    public String getFilename() {
        return this.filename;
    }

    public Integer getFieldCount() {
        return fieldCount;
    }

	/**
	 * @see java.lang.Enum#toString()
	 */
	@Override
	public String toString() {
		return filename;
	}
}
