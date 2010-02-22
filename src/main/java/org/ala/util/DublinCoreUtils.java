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
package org.ala.util;

import java.io.File;
import java.io.FileReader;

import org.ala.repository.Predicates;
import au.com.bytecode.opencsv.CSVReader;

/**
 * Utilities for extracting information from the dublin core files.
 *
 * @author Dave Martin (David.Martin@csiro.au)
 */
public class DublinCoreUtils {

	/**
	 * Retrieve the content type (mime type) for the supplied directory path.
	 * e.g. by reading the file /data/bie/1/1/dc
	 * 
	 * @param repositoryDir
	 * @return
	 * @throws Exception
	 */
	public static String getContentType(String repositoryDir) throws Exception {
		
		String filePath = repositoryDir+File.separator+FileType.DC.toString();
		File file = new File(filePath);
		if(!file.exists()){
			return null;
		}
//		TabReader tabReader = new TabReader(filePath);
		CSVReader csvReader = new CSVReader(new FileReader(filePath), '\t','\"',0);
		
		String[] columns = null;
		
		while((columns = csvReader.readNext())!=null){
			if(columns.length==2){
				if(Predicates.DC_FORMAT.toString().equals(columns[0])){
					return columns[1];
				}
			}
		}
		return null;
	}
}
